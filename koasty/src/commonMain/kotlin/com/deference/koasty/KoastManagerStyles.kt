package com.deference.koasty

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/*
 * Shortcuts for the built-in KoastStyle presets. They are plain extensions
 * over KoastManager.show, so your own styled koasts can be written the
 * same way:
 *
 *     fun KoastManager.showMyVeryOwnKoast(title: String) =
 *         show(title, style = MyVeryOwnStyle)
 */

/** A green koast for an action that succeeded. See [KoastStyle.Success]. */
fun KoastManager.showSuccess(
    title: String,
    message: String? = null,
    duration: Duration = KoastManager.DEFAULT_DURATION.milliseconds,
    key: String? = null
): Long = show(title, message, duration = duration, key = key, style = KoastStyle.Success)

/** A red koast for an action that failed. See [KoastStyle.Error]. */
fun KoastManager.showError(
    title: String,
    message: String? = null,
    duration: Duration = KoastManager.DEFAULT_DURATION.milliseconds,
    key: String? = null
): Long = show(title, message, duration = duration, key = key, style = KoastStyle.Error)

/** An amber koast for something that needs attention. See [KoastStyle.Warning]. */
fun KoastManager.showWarning(
    title: String,
    message: String? = null,
    duration: Duration = KoastManager.DEFAULT_DURATION.milliseconds,
    key: String? = null
): Long = show(title, message, duration = duration, key = key, style = KoastStyle.Warning)

/** A blue koast for neutral information. See [KoastStyle.Info]. */
fun KoastManager.showInfo(
    title: String,
    message: String? = null,
    duration: Duration = KoastManager.DEFAULT_DURATION.milliseconds,
    key: String? = null
): Long = show(title, message, duration = duration, key = key, style = KoastStyle.Info)

/**
 * A small pill at the bottom center that times out on its own and can't
 * be swiped or clicked. See [KoastStyle.Toast].
 */
fun KoastManager.showToast(
    text: String,
    duration: Duration = KoastManager.DEFAULT_DURATION.milliseconds,
    key: String? = null
): Long = show(
    title = text,
    duration = duration,
    dismissible = false,
    key = key,
    style = KoastStyle.Toast
)
