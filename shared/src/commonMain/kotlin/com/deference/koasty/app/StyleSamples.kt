package com.deference.koasty.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deference.koasty.KoastManager
import com.deference.koasty.KoastPosition
import com.deference.koasty.KoastStyle
import com.deference.koasty.showError
import com.deference.koasty.showInfo
import com.deference.koasty.showSuccess
import com.deference.koasty.showToast
import com.deference.koasty.showWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/*
 * A koast designed by the app itself: its own look, its own position and
 * its own call site, written as a plain extension over show().
 */
private val MyVeryOwnStyle = KoastStyle(
    position = KoastPosition.TOP_END,
    compact = true,
    maxWidth = 320.dp,
    containerColor = Color(0xFF6A1B9A),
    contentColor = Color.White,
    shape = RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp),
    outerPadding = PaddingValues(16.dp),
    titleStyle = TextStyle(fontWeight = FontWeight.Black, fontSize = 18.sp),
    swipeUpEnabled = false,
)

internal fun KoastManager.showMyVeryOwnKoast(title: String, message: String? = null): Long =
    show(title, message, duration = 5.seconds, style = MyVeryOwnStyle)

/** Buttons for the "Per-koast styles" section of the demo, label to action. */
internal fun styleSamples(
    koastManager: KoastManager,
    scope: CoroutineScope,
): List<Pair<String, () -> Unit>> = listOf(
    "Success" to {
        koastManager.showSuccess("Saved", "Your changes were stored")
    },
    "Error" to {
        koastManager.showError("Upload failed", "Check your connection and try again")
    },
    "Warning" to {
        koastManager.showWarning("Low storage", "Less than 1 GB left")
    },
    "Info" to {
        koastManager.showInfo("New version", "Version 2.0 is available")
    },
    "Toast" to {
        koastManager.showToast("Copied to clipboard")
    },
    "Banner + toast" to {
        koastManager.showPersistent(
            "You're offline",
            "Persistent banner; the toast doesn't wait for it",
            key = "offline",
            style = KoastStyle.Warning
        )
        koastManager.showToast("Toasts have their own lane")
    },
    "Dismiss banner" to {
        koastManager.queue.firstOrNull { it.key == "offline" }?.let { koastManager.dismiss(it.id) }
    },
    "Info → Success (keyed)" to {
        koastManager.showUntilDismissed(
            "Uploading…",
            "Turns green in 2 seconds",
            key = "status",
            style = KoastStyle.Info
        )
        scope.launch {
            delay(2.seconds)
            koastManager.showSuccess("Uploaded", "Same koast, new style", key = "status")
        }
    },
    "My very own koast" to {
        koastManager.showMyVeryOwnKoast("Very own koast", "Defined in the app with a custom KoastStyle")
    },
)
