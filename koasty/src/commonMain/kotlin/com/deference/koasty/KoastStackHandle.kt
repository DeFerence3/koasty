package com.deference.koasty

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * What a stack handle needs to draw itself.
 *
 * @property moreCount koasts in the queue besides the one in front.
 * @property expanded whether the stack is currently spread out as a list.
 */
@Immutable
class KoastStackHandleState internal constructor(
    val moreCount: Int,
    val expanded: Boolean,
    private val onToggle: () -> Unit
) {
    /**
     * Expand a collapsed stack, or collapse an expanded one.
     */
    fun toggle() = onToggle()
}

/**
 * The default handle for a [KoastDisplayMode.Stacked] stack: a small pill
 * reading "n more koasts", or "Show less" while expanded.
 */
@Composable
fun KoastStackHandle(
    state: KoastStackHandleState,
    config: KoastyHostConfig,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = state::toggle,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = config.containerColor,
        contentColor = config.contentColor,
        shadowElevation = config.shadowElevation / 2
    ) {
        Text(
            text = stackHandleLabel(state.moreCount, state.expanded),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

internal fun stackHandleLabel(moreCount: Int, expanded: Boolean): String = when {
    expanded -> "Show less"
    moreCount == 1 -> "1 more koast"
    else -> "$moreCount more koasts"
}
