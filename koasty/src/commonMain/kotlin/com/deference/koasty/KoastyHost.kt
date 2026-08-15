package com.deference.koasty

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

@Composable
fun KoastyHost(
    koastManager: KoastManager,
    modifier: Modifier = Modifier,
    config: KoastyHostConfig = KoastyHostConfig(),
    leadingContent: (@Composable RowScope.(KoastRequest) -> Unit)? = null,
    koastContent: (@Composable (KoastRequest) -> Unit)? = null
) {
    val managerState by koastManager.state.collectAsState()
    val current = managerState.current

    /*
     * We keep the last koast rendered while it animates out.
     * Otherwise, setting manager.current = null would remove
     * the composable instantly.
     */
    var renderedKoast by remember {
        mutableStateOf<KoastRequest?>(null)
    }

    var measuredSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    /*
     * 0 = above the screen
     * 1 = normal visible position
     */
    val visibilityProgress = remember {
        Animatable(0f)
    }

    var hiddenDistancePx by remember {
        mutableFloatStateOf(1f)
    }

    /*
     * Gesture offsets are separate from the entrance animation.
     */
    var dragX by remember {
        mutableFloatStateOf(0f)
    }

    var dragY by remember {
        mutableFloatStateOf(0f)
    }

    var gestureAlpha by remember {
        mutableFloatStateOf(1f)
    }

    var gestureJob by remember {
        mutableStateOf<Job?>(null)
    }

    val coroutineScope = rememberCoroutineScope()

    /*
     * Handles:
     * - a new koast
     * - automatic timeout
     * - programmatic dismissal
     */
    LaunchedEffect(current?.id) {
        gestureJob?.cancel()

        val incoming = current

        if (incoming != null) {

            renderedKoast = incoming

            dragX = 0f
            dragY = 0f
            gestureAlpha = 1f

            visibilityProgress.snapTo(0f)

            /*
             * Give the koast one frame to measure itself.
             * That way it starts fully above the screen even
             * when custom content changes its height.
             */
            withFrameNanos { }

            hiddenDistancePx = max(
                measuredSize.height.toFloat(),
                1f
            )

            visibilityProgress.animateTo(
                targetValue = 1f,
                animationSpec = config.enterAnimationSpec
            )

            when (val lifetime = incoming.lifetime) {
                is KoastLifetime.Timed -> {
                    delay(lifetime.millis)

                    koastManager.dismiss(
                        id = incoming.id
                    )
                }

                KoastLifetime.UntilDismissed -> {
                    // Stay visible forever.
                }
            }

        } else if (renderedKoast != null) {

            visibilityProgress.animateTo(
                targetValue = 0f,
                animationSpec = config.exitAnimationSpec
            )

            renderedKoast = null

            dragX = 0f
            dragY = 0f
            gestureAlpha = 1f
        }
    }

    val koast = renderedKoast ?: return

    /*
     * Only the currently-active koast should receive input.
     * During exit animation it becomes non-interactive.
     */
    val interactive =
        current?.id == koast.id

    fun settleBack() {
        gestureJob?.cancel()

        val startX = dragX
        val startY = dragY
        val startAlpha = gestureAlpha

        gestureJob = coroutineScope.launch {
            coroutineScope {
                launch {
                    animateFloat(
                        from = startX,
                        to = 0f,
                        spec = config.settleAnimationSpec
                    ) {
                        dragX = it
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = 0f,
                        spec = config.settleAnimationSpec
                    ) {
                        dragY = it
                    }
                }

                launch {
                    animateFloat(
                        from = startAlpha,
                        to = 1f,
                        spec = config.settleAnimationSpec
                    ) {
                        gestureAlpha = it
                    }
                }
            }
        }
    }

    fun dismissHorizontally() {
        gestureJob?.cancel()

        val width = measuredSize.width
            .toFloat()
            .coerceAtLeast(1f)

        val targetX = if (dragX < 0f) {
            -width * 1.15f
        } else {
            width * 1.15f
        }

        val startX = dragX
        val startY = dragY
        val startAlpha = gestureAlpha

        gestureJob = coroutineScope.launch {
            coroutineScope {
                launch {
                    animateFloat(
                        from = startX,
                        to = targetX,
                        spec = config.exitAnimationSpec
                    ) {
                        dragX = it
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = 0f,
                        spec = config.exitAnimationSpec
                    ) {
                        dragY = it
                    }
                }

                launch {
                    animateFloat(
                        from = startAlpha,
                        to = 0f,
                        spec = config.exitAnimationSpec
                    ) {
                        gestureAlpha = it
                    }
                }
            }

            koastManager.dismiss(koast.id)
        }
    }

    fun dismissUpward() {
        gestureJob?.cancel()

        val height = measuredSize.height
            .toFloat()
            .coerceAtLeast(1f)

        val startX = dragX
        val startY = dragY
        val startAlpha = gestureAlpha

        gestureJob = coroutineScope.launch {
            coroutineScope {
                launch {
                    animateFloat(
                        from = startX,
                        to = 0f,
                        spec = config.exitAnimationSpec
                    ) {
                        dragX = it
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = -height * 1.15f,
                        spec = config.exitAnimationSpec
                    ) {
                        dragY = it
                    }
                }

                launch {
                    animateFloat(
                        from = startAlpha,
                        to = 0f,
                        spec = config.exitAnimationSpec
                    ) {
                        gestureAlpha = it
                    }
                }
            }

            koastManager.dismiss(koast.id)
        }
    }

    val gestureModifier =
        if (
            interactive &&
            koast.dismissible &&
            (
                    config.horizontalSwipeEnabled ||
                            config.swipeUpEnabled
                    )
        ) {
            Modifier.pointerInput(
                koast.id,
                config
            ) {
                detectDragGestures(
                    onDragStart = {
                        gestureJob?.cancel()
                    },

                    onDragCancel = {
                        settleBack()
                    },

                    onDragEnd = {
                        val width = measuredSize.width
                            .toFloat()
                            .coerceAtLeast(1f)

                        val height = measuredSize.height
                            .toFloat()
                            .coerceAtLeast(1f)

                        val horizontalProgress =
                            if (config.horizontalSwipeEnabled) {
                                abs(dragX) / width
                            } else {
                                0f
                            }

                        val upwardProgress =
                            if (config.swipeUpEnabled) {
                                (-dragY)
                                    .coerceAtLeast(0f) / height
                            } else {
                                0f
                            }

                        val horizontalDismiss =
                            horizontalProgress >=
                                    config.horizontalDismissThresholdFraction

                        val upwardDismiss =
                            upwardProgress >=
                                    config.upDismissThresholdFraction

                        when {
                            horizontalDismiss &&
                                    upwardDismiss -> {

                                /*
                                 * If the user moved diagonally,
                                 * dismiss in whichever direction
                                 * passed its threshold further.
                                 */
                                val horizontalScore =
                                    horizontalProgress /
                                            config
                                                .horizontalDismissThresholdFraction
                                                .coerceAtLeast(0.01f)

                                val verticalScore =
                                    upwardProgress /
                                            config
                                                .upDismissThresholdFraction
                                                .coerceAtLeast(0.01f)

                                if (horizontalScore >= verticalScore) {
                                    dismissHorizontally()
                                } else {
                                    dismissUpward()
                                }
                            }

                            horizontalDismiss -> {
                                dismissHorizontally()
                            }

                            upwardDismiss -> {
                                dismissUpward()
                            }

                            else -> {
                                settleBack()
                            }
                        }
                    },

                    onDrag = { change, dragAmount ->
                        change.consume()

                        if (config.horizontalSwipeEnabled) {
                            dragX += dragAmount.x
                        }

                        if (config.swipeUpEnabled) {
                            /*
                             * Don't allow dragging downward.
                             * Only upward movement.
                             */
                            dragY = (
                                    dragY + dragAmount.y
                                    ).coerceAtMost(0f)
                        }

                        val width = measuredSize.width
                            .toFloat()
                            .coerceAtLeast(1f)

                        val height = measuredSize.height
                            .toFloat()
                            .coerceAtLeast(1f)

                        val horizontalProgress =
                            abs(dragX) / width

                        val verticalProgress =
                            (-dragY)
                                .coerceAtLeast(0f) / height

                        val progress = max(
                            horizontalProgress,
                            verticalProgress
                        )

                        /*
                         * Fade slightly as the koast moves away.
                         */
                        gestureAlpha = (
                                1f - progress * 0.65f
                                ).coerceIn(
                                minimumValue = 0.25f,
                                maximumValue = 1f
                            )
                    }
                )
            }
        } else {
            Modifier
        }

    val clickableModifier =
        if (
            interactive &&
            koast.onClick != null
        ) {
            Modifier.clickable {
                koast.onClick.invoke()

                if (koast.dismissOnClick) {
                    koastManager.dismiss(koast.id)
                }
            }
        } else {
            Modifier
        }

    /*
     * Slide from above the screen.
     */
    val entranceY =
        -hiddenDistancePx *
                (1f - visibilityProgress.value)

    val finalAlpha =
        (
                visibilityProgress.value *
                        gestureAlpha
                ).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1000f)
            .graphicsLayer {
                translationX = dragX
                translationY = entranceY + dragY
                alpha = finalAlpha
            }
            .onSizeChanged {
                measuredSize = it
            }
            .then(gestureModifier)
            .then(clickableModifier),
        color = config.containerColor,
        contentColor = config.contentColor,
        shape = config.shape,
        shadowElevation = config.shadowElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = config.minHeight
                )
                .padding(
                    config.contentPadding
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            if (koastContent != null) {
                koastContent(koast)
            } else {
                DefaultKoastContent(
                    koast = koast,
                    config = config,
                    leadingContent = leadingContent
                )
            }
        }
    }
}

@Composable
private fun DefaultKoastContent(
    koast: KoastRequest,
    config: KoastyHostConfig,
    leadingContent: (@Composable RowScope.(KoastRequest) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            leadingContent(koast)
            Spacer(
                Modifier.width(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = koast.title,
                style = MaterialTheme.typography
                    .titleMedium
                    .merge(config.titleStyle)
            )

            if (!koast.message.isNullOrBlank()) {
                Spacer(
                    Modifier.size(3.dp)
                )
                Text(
                    text = koast.message,
                    style = MaterialTheme.typography
                        .bodyMedium
                        .merge(config.messageStyle),
                    color = config.contentColor.copy(
                        alpha = 0.90f
                    )
                )
            }
        }
    }
}

private suspend fun animateFloat(
    from: Float,
    to: Float,
    spec: FiniteAnimationSpec<Float>,
    onValue: (Float) -> Unit
) {
    Animatable(from)
        .animateTo(
            targetValue = to,
            animationSpec = spec
        ) {
            onValue(value)
        }
}
