package com.deference.koasty.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.deference.koasty.KoastyHostConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {
    private val _config = MutableStateFlow(KoastyHostConfig())
    val config = _config.asStateFlow()

    fun onAction(action: AppAction) {
        when (action) {
            AppAction.Compact -> _config.update { it.withCompact(!it.compact) }
            AppAction.RespectSafeArea -> _config.update { it.copy(respectSafeArea = !it.respectSafeArea) }
            AppAction.HorizontalSwipe -> _config.update { it.copy(horizontalSwipeEnabled = !it.horizontalSwipeEnabled) }
            AppAction.SwipeUp -> _config.update { it.copy(swipeUpEnabled = !it.swipeUpEnabled) }
            AppAction.CompactDismissButton -> _config.update { it.copy(showCompactDismissButton = !it.showCompactDismissButton) }
            is AppAction.DisplayMode -> _config.update { it.copy(displayMode = action.mode) }
            is AppAction.Position -> _config.update { it.copy(position = action.position) }
            is AppAction.EdgePadding -> _config.update { it.copy(edgePadding = action.value.dp) }
            is AppAction.MaxWidth -> _config.update { it.copy(maxWidth = action.value.dp) }
            is AppAction.MinHeight -> _config.update { it.copy(minHeight = action.value.dp) }
        }
    }

    private fun KoastyHostConfig.withCompact(compact: Boolean): KoastyHostConfig = copy(
        compact = compact,
        shape = if (compact) RoundedCornerShape(12.dp) else RectangleShape,
        minHeight = if (compact) 68.dp else 112.dp,
    )
}
