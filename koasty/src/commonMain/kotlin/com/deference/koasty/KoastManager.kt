package com.deference.koasty

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class KoastManager {

    private val _state = MutableStateFlow(KoastManagerState())

    internal val state: StateFlow<KoastManagerState> =
        _state.asStateFlow()

    val currentKoast: KoastRequest?
        get() = _state.value.current

    val isShowing: Boolean
        get() = currentKoast != null

    /**
     * Every pending koast in FIFO order, starting with [currentKoast].
     */
    val queue: List<KoastRequest>
        get() = _state.value.queue

    /**
     * Normal timed koast.
     *
     * Koasts are queued first in, first out, in one lane per position.
     * If another koast is already showing at the same position, this one
     * waits until every koast queued there before it has been dismissed.
     *
     * By default,:
     * - disappears after [duration] once it reaches the front
     * - can be swiped away
     *
     * @param key identifies a koast that later calls can update. If a
     * koast with the same key is still queued or showing, this one
     * replaces it in place: same queue position, same id, new content,
     * and its lifetime starts over. Without a match, or with a null key,
     * the koast is queued as usual. Every `show*` method takes a key.
     * @param style overrides the host config for this koast only: colors,
     * shape, position, gestures or the whole content. `null` uses the host
     * config. Every `show*` method takes a style. See [KoastStyle].
     * @return the id of the queued koast, usable with [dismiss]. When a
     * keyed koast is replaced, the id stays the same.
     */
    fun show(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        dismissible: Boolean = true,
        dismissOnClick: Boolean = false,
        onClick: (() -> Unit)? = null,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return publish(
            title = title,
            message = message,
            lifetime = KoastLifetime.Timed(duration),
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick,
            key = key,
            style = style
        )
    }

    /**
     * Alias if you prefer:
     *
     * koastManager.koast(...)
     */
    fun koast(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            key = key,
            style = style
        )
    }

    /**
     * Explicitly dismissible timed koast.
     */
    fun showDismissible(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = true,
            key = key,
            style = style
        )
    }

    /**
     * User cannot swipe this away.
     *
     * It still automatically disappears after durationMillis.
     */
    fun showNonDismissible(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = false,
            key = key,
            style = style
        )
    }

    /**
     * Clickable koast.
     */
    fun showClickable(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        dismissible: Boolean = true,
        dismissOnClick: Boolean = true,
        key: String? = null,
        style: KoastStyle? = null,
        onClick: () -> Unit
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick,
            key = key,
            style = style
        )
    }

    /**
     * Does NOT disappear automatically.
     *
     * If dismissible = true:
     * swipe or koastManager.dismiss() can remove it.
     *
     * If dismissible = false:
     * only koastManager.dismiss() removes it.
     */
    fun showUntilDismissed(
        title: String,
        message: String? = null,
        dismissible: Boolean = true,
        dismissOnClick: Boolean = false,
        onClick: (() -> Unit)? = null,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return publish(
            title = title,
            message = message,
            lifetime = KoastLifetime.UntilDismissed,
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick,
            key = key,
            style = style
        )
    }

    /**
     * Persistent koast which can only be removed programmatically.
     */
    fun showPersistent(
        title: String,
        message: String? = null,
        key: String? = null,
        style: KoastStyle? = null
    ): Long {
        return showUntilDismissed(
            title = title,
            message = message,
            dismissible = false,
            key = key,
            style = style
        )
    }

    /**
     * Dismiss the oldest koast. It is the front of its position's lane;
     * the next koast queued at that position, if any, takes its place.
     */
    fun dismiss() {
        _state.update { old ->
            if (old.queue.isEmpty()) old
            else old.copy(queue = old.queue.drop(1))
        }
    }

    /**
     * Dismiss the koast with this [id], whether it is in front or still
     * waiting in the queue.
     *
     * Ids of koasts that were already dismissed are ignored, so an old
     * timeout can never dismiss a newer koast.
     *
     * @return true if a koast was removed.
     */
    fun dismiss(id: Long): Boolean {
        var removed = false

        _state.update { old ->
            val remaining = old.queue.filterNot { it.id == id }
            removed = remaining.size != old.queue.size

            if (removed) old.copy(queue = remaining) else old
        }

        return removed
    }

    /**
     * Dismiss [koast] only if the queue still holds this exact request.
     *
     * Used by the host's lifetime countdown: if a keyed koast was replaced
     * while its timer was running, the replacement shares the id but is a
     * different request, so the old timer leaves it alone.
     */
    internal fun dismiss(koast: KoastRequest): Boolean {
        var removed = false

        _state.update { old ->
            val remaining = old.queue.filterNot { it === koast }
            removed = remaining.size != old.queue.size

            if (removed) old.copy(queue = remaining) else old
        }

        return removed
    }

    /**
     * Dismiss the koast in front and clear everything queued behind it.
     */
    fun dismissAll() {
        _state.update { old ->
            if (old.queue.isEmpty()) old
            else old.copy(queue = emptyList())
        }
    }

    private fun publish(
        title: String,
        message: String?,
        lifetime: KoastLifetime,
        dismissible: Boolean,
        dismissOnClick: Boolean,
        onClick: (() -> Unit)?,
        key: String?,
        style: KoastStyle?
    ): Long {
        var emittedId = 0L

        _state.update { old ->
            fun request(id: Long) = KoastRequest(
                id = id,
                title = title,
                message = message,
                lifetime = lifetime,
                dismissible = dismissible,
                dismissOnClick = dismissOnClick,
                onClick = onClick,
                key = key,
                style = style
            )

            val replaceIndex =
                if (key == null) -1
                else old.queue.indexOfFirst { it.key == key }

            if (replaceIndex >= 0) {
                // Same key: update in place, keeping position and id.
                val id = old.queue[replaceIndex].id
                emittedId = id

                old.copy(
                    queue = old.queue.toMutableList().apply {
                        set(replaceIndex, request(id))
                    }
                )
            } else {
                val id = old.sequence + 1L
                emittedId = id

                KoastManagerState(
                    sequence = id,
                    queue = old.queue + request(id)
                )
            }
        }

        return emittedId
    }

    companion object {
        const val DEFAULT_DURATION: Long = 3_500L
    }
}
