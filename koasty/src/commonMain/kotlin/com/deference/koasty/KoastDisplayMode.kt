package com.deference.koasty

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Controls how [KoastyHost] presents the [KoastManager] queue.
 *
 * In every mode the queue is first in, first out: only the front koast
 * counts down its lifetime and receives input. When it leaves, the next
 * queued koast takes its place.
 */
sealed interface KoastDisplayMode {

    /**
     * Queued koasts are layered behind the front koast like a deck of
     * cards. When the front koast leaves, the next card moves forward.
     *
     * @param maxVisible how many cards are drawn, including the front one.
     * Further koasts wait in the queue without being drawn.
     * @param peekOffset how far each card behind peeks out past the one
     * in front of it.
     * @param scaleStep how much smaller each card behind is drawn.
     * @param expandable whether a handle is shown next to the stack.
     * Clicking it spreads the whole queue out as a scrollable list and
     * clicking it again collapses it back into a stack.
     * @param expandedSpacing the gap between cards while expanded.
     */
    data class Stacked(
        val maxVisible: Int = 4,
        val peekOffset: Dp = 10.dp,
        val scaleStep: Float = 0.05f,
        val expandable: Boolean = true,
        val expandedSpacing: Dp = 8.dp
    ) : KoastDisplayMode {
        init {
            require(maxVisible >= 1)
            require(peekOffset >= 0.dp)
            require(scaleStep in 0f..<1f)
            require(expandedSpacing >= 0.dp)
        }
    }

    companion object {
        /**
         * One koast at a time: a [Stacked] stack with every stack effect
         * turned off. Only the front koast is drawn; the next one stays
         * hidden behind it and fades in as the front koast leaves.
         */
        val Sequential: Stacked = Stacked(
            maxVisible = 1,
            peekOffset = 0.dp,
            scaleStep = 0f,
            expandable = false
        )
    }
}
