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
     * Koasts are queued first in, first out. If another koast is already
     * showing, this one waits until every koast queued before it has been
     * dismissed.
     *
     * By default,:
     * - disappears after [duration] once it reaches the front
     * - can be swiped away
     *
     * @return the id of the queued koast, usable with [dismiss].
     */
    fun show(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds,
        dismissible: Boolean = true,
        dismissOnClick: Boolean = false,
        onClick: (() -> Unit)? = null
    ): Long {
        return publish(
            title = title,
            message = message,
            lifetime = KoastLifetime.Timed(duration),
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick
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
        duration: Duration = DEFAULT_DURATION.milliseconds
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration
        )
    }

    /**
     * Explicitly dismissible timed koast.
     */
    fun showDismissible(
        title: String,
        message: String? = null,
        duration: Duration = DEFAULT_DURATION.milliseconds
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = true
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
        duration: Duration = DEFAULT_DURATION.milliseconds
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = false
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
        onClick: () -> Unit
    ): Long {
        return show(
            title = title,
            message = message,
            duration = duration,
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick
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
        onClick: (() -> Unit)? = null
    ): Long {
        return publish(
            title = title,
            message = message,
            lifetime = KoastLifetime.UntilDismissed,
            dismissible = dismissible,
            dismissOnClick = dismissOnClick,
            onClick = onClick
        )
    }

    /**
     * Persistent koast which can only be removed programmatically.
     */
    fun showPersistent(
        title: String,
        message: String? = null
    ): Long {
        return showUntilDismissed(
            title = title,
            message = message,
            dismissible = false
        )
    }

    /**
     * Dismiss the koast currently in front. The next queued koast,
     * if any, takes its place.
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
        onClick: (() -> Unit)?
    ): Long {
        var emittedId = 0L

        _state.update { old ->
            val id = old.sequence + 1L
            emittedId = id

            KoastManagerState(
                sequence = id,
                queue = old.queue + KoastRequest(
                    id = id,
                    title = title,
                    message = message,
                    lifetime = lifetime,
                    dismissible = dismissible,
                    dismissOnClick = dismissOnClick,
                    onClick = onClick
                )
            )
        }

        return emittedId
    }

    companion object {
        const val DEFAULT_DURATION: Long = 3_500L
    }
}
