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
     * One koast at a time. The next queued koast enters after the
     * current one has finished animating out.
     */
    data object Sequential : KoastDisplayMode

    /**
     * Queued koasts are layered behind the front koast like a deck of
     * cards. When the front koast leaves, the next card moves forward.
     *
     * @param maxVisible how many cards are drawn, including the front one.
     * Further koasts wait in the queue without being drawn.
     * @param peekOffset how far each card behind peeks out past the one
     * in front of it.
     * @param scaleStep how much smaller each card behind is drawn.
     */
    data class Stacked(
        val maxVisible: Int = 4,
        val peekOffset: Dp = 10.dp,
        val scaleStep: Float = 0.05f
    ) : KoastDisplayMode {
        init {
            require(maxVisible >= 1)
            require(peekOffset >= 0.dp)
            require(scaleStep in 0f..<1f)
        }
    }
}
