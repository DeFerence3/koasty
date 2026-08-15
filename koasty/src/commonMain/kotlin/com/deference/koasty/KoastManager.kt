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
     * Normal timed koast.
     *
     * By default,:
     * - disappears after [duration]
     * - can be swiped away
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
     * Dismiss whatever is currently visible.
     */
    fun dismiss() {
        dismiss(id = null)
    }

    /**
     * Dismiss only if this exact koast is still visible.
     *
     * Useful for timers so an old timeout cannot accidentally
     * dismiss a newer koast.
     */
    internal fun dismiss(id: Long?) {
        _state.update { old ->
            val current = old.current

            if (
                current == null ||
                (id != null && current.id != id)
            ) {
                old
            } else {
                old.copy(current = null)
            }
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
                current = KoastRequest(
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
