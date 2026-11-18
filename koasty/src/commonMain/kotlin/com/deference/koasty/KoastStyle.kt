package com.deference.koasty

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How one koast looks, where it appears and which gestures it accepts.
 *
 * Pass it to any `show*` method with `style = ...`. Every property is
 * optional: `null` means "use the value from [KoastyHostConfig]", so a
 * style only needs to set what it changes.
 *
 * Koasts are grouped by [position]. Each position has its own first-in,
 * first-out lane, so a koast at the bottom never waits behind one at the
 * top.
 *
 * Use one of the presets ([Success], [Error], [Warning], [Info], [Toast]),
 * derive one with `copy(...)`, or build your own. To give a style its own
 * call site, write an extension:
 *
 * ```kotlin
 * val Celebration = KoastStyle(containerColor = Color(0xFF6A1B9A))
 *
 * fun KoastManager.showCelebration(title: String, message: String? = null) =
 *     show(title, message, style = Celebration)
 * ```
 *
 * @param position where the koast appears.
 * @param compact width capped at [maxWidth] instead of filling the lane.
 * @param outerPadding space around the card, inside its lane. The
 * [Toast] preset uses it to sit above the bottom edge.
 * @param stackable `false` keeps this koast out of stacks: while it is
 * queued, its lane shows one koast at a time
 * ([KoastDisplayMode.Sequential]) with no peek, scale or expand handle,
 * whatever the host's display mode. The [Toast] preset sets it.
 * @param content draws the inside of the card, replacing both the
 * provider's `koastContent` and [DefaultKoastContent].
 */
data class KoastStyle(
    val position: KoastPosition? = null,
    val compact: Boolean? = null,
    val maxWidth: Dp? = null,
    val minHeight: Dp? = null,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val shape: Shape? = null,
    val shadowElevation: Dp? = null,
    val contentPadding: PaddingValues? = null,
    val outerPadding: PaddingValues? = null,
    val titleStyle: TextStyle? = null,
    val messageStyle: TextStyle? = null,
    val horizontalSwipeEnabled: Boolean? = null,
    val swipeUpEnabled: Boolean? = null,
    val stackable: Boolean? = null,
    val content: (@Composable (KoastRequest) -> Unit)? = null
) {
    init {
        require(maxWidth == null || maxWidth > 0.dp)
        require(minHeight == null || minHeight >= 0.dp)
    }

    companion object {
        /** Green, for an action that succeeded. */
        val Success: KoastStyle = KoastStyle(
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White
        )

        /** Red, for an action that failed. */
        val Error: KoastStyle = KoastStyle(
            containerColor = Color(0xFFC62828),
            contentColor = Color.White
        )

        /** Amber, for something that needs attention. */
        val Warning: KoastStyle = KoastStyle(
            containerColor = Color(0xFFF9A825),
            contentColor = Color(0xFF212121)
        )

        /** Blue, for neutral information. */
        val Info: KoastStyle = KoastStyle(
            containerColor = Color(0xFF1565C0),
            contentColor = Color.White
        )

        /**
         * A small pill at the bottom center, like a platform toast: only
         * the title is shown, and it can't be swiped away.
         */
        val Toast: KoastStyle = KoastStyle(
            position = KoastPosition.BOTTOM_CENTER,
            compact = true,
            minHeight = 0.dp,
            containerColor = Color(0xFF323232),
            contentColor = Color.White,
            shape = CircleShape,
            shadowElevation = 4.dp,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            outerPadding = PaddingValues(bottom = 64.dp),
            horizontalSwipeEnabled = false,
            swipeUpEnabled = false,
            stackable = false,
            content = { koast -> ToastContent(koast) }
        )
    }
}

@Composable
private fun ToastContent(koast: KoastRequest) {
    Text(
        text = koast.title,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

/** A [KoastStyle] with every gap filled in from the host config. */
internal data class ResolvedKoastStyle(
    val position: KoastPosition,
    val compact: Boolean,
    val maxWidth: Dp,
    val minHeight: Dp,
    val containerColor: Color,
    val contentColor: Color,
    val shape: Shape,
    val shadowElevation: Dp,
    val contentPadding: PaddingValues,
    val outerPadding: PaddingValues,
    val titleStyle: TextStyle,
    val messageStyle: TextStyle,
    val horizontalSwipeEnabled: Boolean,
    val swipeUpEnabled: Boolean,
    val stackable: Boolean,
    val content: (@Composable (KoastRequest) -> Unit)?
)

internal fun KoastyHostConfig.resolve(style: KoastStyle?): ResolvedKoastStyle =
    ResolvedKoastStyle(
        position = style?.position ?: position,
        compact = style?.compact ?: compact,
        maxWidth = style?.maxWidth ?: maxWidth,
        minHeight = style?.minHeight ?: minHeight,
        containerColor = style?.containerColor ?: containerColor,
        contentColor = style?.contentColor ?: contentColor,
        shape = style?.shape ?: shape,
        shadowElevation = style?.shadowElevation ?: shadowElevation,
        contentPadding = style?.contentPadding ?: contentPadding,
        outerPadding = style?.outerPadding ?: PaddingValues(0.dp),
        titleStyle = style?.titleStyle ?: titleStyle,
        messageStyle = style?.messageStyle ?: messageStyle,
        horizontalSwipeEnabled = style?.horizontalSwipeEnabled ?: horizontalSwipeEnabled,
        swipeUpEnabled = style?.swipeUpEnabled ?: swipeUpEnabled,
        stackable = style?.stackable ?: true,
        content = style?.content
    )

/** The koasts that appear at [position], in queue order. */
internal fun List<KoastRequest>.inLane(
    position: KoastPosition,
    config: KoastyHostConfig
): List<KoastRequest> = filter { (it.style?.position ?: config.position) == position }

/**
 * How a lane presents these koasts: the host's display mode, unless one of
 * them opted out of stacking with [KoastStyle.stackable], which turns the
 * lane [KoastDisplayMode.Sequential] for as long as it is queued.
 */
internal fun List<KoastRequest>.laneDisplayMode(
    config: KoastyHostConfig
): KoastDisplayMode.Stacked =
    if (any { it.style?.stackable == false }) {
        KoastDisplayMode.Sequential
    } else {
        when (val mode = config.displayMode) {
            is KoastDisplayMode.Stacked -> mode
        }
    }
