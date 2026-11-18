package com.deference.koasty.app

import com.deference.koasty.KoastDisplayMode
import com.deference.koasty.KoastPosition

sealed interface AppAction {
    data object Compact : AppAction
    data object RespectSafeArea : AppAction
    data object HorizontalSwipe : AppAction
    data object SwipeUp : AppAction
    data object CompactDismissButton : AppAction
    data class DisplayMode(val mode: KoastDisplayMode) : AppAction
    data class Position(val position: KoastPosition) : AppAction
    data class EdgePadding(val value: Float) : AppAction
    data class MaxWidth(val value: Float) : AppAction
    data class MinHeight(val value: Float) : AppAction
}
