package com.deference.koasty

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KoastyHostConfig(

    // Appearance
    val minHeight: Dp = 112.dp,
    val containerColor: Color = Color.Gray,
    val contentColor: Color = Color.White,
    val shape: Shape = RectangleShape,
    val shadowElevation: Dp = 8.dp,

    val contentPadding: PaddingValues = PaddingValues(
        start = 24.dp,
        end = 24.dp,
        top = 20.dp,
        bottom = 4.dp,
    ),

    val titleStyle: TextStyle = TextStyle.Default,
    val messageStyle: TextStyle = TextStyle.Default,

    // Gestures
    val horizontalSwipeEnabled: Boolean = true,
    val swipeUpEnabled: Boolean = true,

    /**
     * Swipe 28% of koast width before dismiss.
     */
    val horizontalDismissThresholdFraction: Float = 0.28f,

    /**
     * Swipe 25% of koast height upward before dismiss.
     */
    val upDismissThresholdFraction: Float = 0.25f,

    // Animations
    val enterAnimationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),

    val exitAnimationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 220
    ),

    val settleAnimationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
) {
    init {
        require(horizontalDismissThresholdFraction in 0f..1f)
        require(upDismissThresholdFraction in 0f..1f)
    }
}
