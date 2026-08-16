package com.deference.koasty

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KoastyHostConfig(

    // Placement and responsive layout
    val position: KoastPosition = defaultKoastPosition(),
    val compact: Boolean = defaultCompactKoastLayout(),
    val maxWidth: Dp = 448.dp,
    val edgePadding: Dp = 16.dp,
    val showCompactDismissButton: Boolean = true,
    val dismissButtonContainerColor: Color = Color(0xFFFFF1F1),
    val dismissButtonContentColor: Color = Color(0xFFE53935),

    // Appearance
    val minHeight: Dp = if (defaultCompactKoastLayout()) 68.dp else 112.dp,
    val containerColor: Color = Color.Gray,
    val contentColor: Color = Color.White,
    val shape: Shape = if (defaultCompactKoastLayout()) RoundedCornerShape(12.dp) else RectangleShape,
    val shadowElevation: Dp = 8.dp,

    val contentPadding: PaddingValues = PaddingValues(20.dp),

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
        require(maxWidth > 0.dp)
        require(edgePadding >= 0.dp)
        require(horizontalDismissThresholdFraction in 0f..1f)
        require(upDismissThresholdFraction in 0f..1f)
    }
}
