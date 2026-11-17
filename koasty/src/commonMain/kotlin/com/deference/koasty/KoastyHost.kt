package com.deference.koasty

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

@Composable
fun KoastyHost(
    koastManager: KoastManager,
    modifier: Modifier = Modifier,
    config: KoastyHostConfig = KoastyHostConfig(),
    koastContent: (@Composable (KoastRequest) -> Unit)? = null
) {
    val managerState by koastManager.state.collectAsState()
    val stacked = config.displayMode as? KoastDisplayMode.Stacked
    val koasts = managerState.queue.map { KoastEntry(it) }.toMutableStateList()
    if (koasts.isEmpty()) return

    var frontHeightPx by remember { mutableIntStateOf(0) }

    val stackHeightPx = remember { Animatable(0f) }
    LaunchedEffect(frontHeightPx) {
        if (frontHeightPx <= 0) return@LaunchedEffect
        if (stackHeightPx.value <= 0f) {
            stackHeightPx.snapTo(frontHeightPx.toFloat())
        } else {
            stackHeightPx.animateTo(
                targetValue = frontHeightPx.toFloat(),
                animationSpec = config.settleAnimationSpec
            )
        }
    }

    val swipe = remember { SwipeProgressState() }
    val frontId = koasts.firstOrNull { !it.exiting }?.koast?.id
    val frontSwipeProgress: () -> Float = {
        if (swipe.ownerId == frontId) swipe.progress else 0f
    }

    Box(
        modifier = modifier
            .then(
                if (config.compact) Modifier.widthIn(max = config.maxWidth)
                else Modifier.fillMaxWidth()
            )
            .zIndex(1000f),
        contentAlignment = config.position.toAlignment()
    ) {
        var activeIndex = 0
        koasts.forEach { entry ->
            val stackIndex = if (entry.exiting) entry.exitIndex else activeIndex++
            key(entry.koast.id) {
                KoastCard(
                    koast = entry.koast,
                    exiting = entry.exiting,
                    stackIndex = stackIndex,
                    stacked = stacked,
                    stackHeightPx = { stackHeightPx.value },
                    frontSwipeProgress = frontSwipeProgress,
                    onSwipeProgress = { swipe.report(entry.koast.id, it) },
                    onFrontHeightChanged = { frontHeightPx = it },
                    onExited = { koasts.remove(entry) },
                    koastManager = koastManager,
                    config = config,
                    koastContent = koastContent,
                    modifier = Modifier.zIndex(
                        -stackIndex.toFloat() + if (entry.exiting) 0.5f else 0f
                    )
                )
            }
        }
    }
}

@Composable
private fun KoastCard(
    koast: KoastRequest,
    exiting: Boolean,
    stackIndex: Int,
    stacked: KoastDisplayMode.Stacked?,
    stackHeightPx: () -> Float,
    frontSwipeProgress: () -> Float,
    onSwipeProgress: (Float) -> Unit,
    onFrontHeightChanged: (Int) -> Unit,
    onExited: () -> Unit,
    koastManager: KoastManager,
    config: KoastyHostConfig,
    koastContent: (@Composable (KoastRequest) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var measuredSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    /*
     * Height the content wants on its own, measured even while
     * the card is squeezed to the stack height.
     */
    var naturalHeightPx by remember {
        mutableIntStateOf(0)
    }

    /*
     * 0 = outside the screen edge
     * 1 = normal visible position
     */
    val visibilityProgress = remember { Animatable(0f) }

    var entered by remember { mutableStateOf(false) }

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
    val currentOnExited by rememberUpdatedState(onExited)
    val currentOnSwipeProgress by rememberUpdatedState(onSwipeProgress)

    /*
     * Only the front koast counts down, takes input and
     * decides the height of the cards behind it.
     */
    val isFront = stackIndex == 0 && !exiting

    LaunchedEffect(Unit) {
        /*
         * Give the koast one frame to measure itself.
         * That way it starts fully outside the screen even
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

        entered = true
    }

    LaunchedEffect(exiting) {
        if (!exiting) return@LaunchedEffect

        /*
         * A swiped-away koast is already invisible, so there is
         * no need to keep the next one waiting for another animation.
         */
        if (gestureAlpha > 0.01f) {
            visibilityProgress.animateTo(
                targetValue = 0f,
                animationSpec = config.exitAnimationSpec
            )
        }

        currentOnExited()
    }
    LaunchedEffect(isFront) {
        if (!isFront) return@LaunchedEffect
        val lifetime = koast.lifetime as? KoastLifetime.Timed ?: return@LaunchedEffect

        snapshotFlow { entered }.first { it }
        delay(lifetime.millis)
        koastManager.dismiss(koast.id)
    }

    LaunchedEffect(isFront, measuredSize.height) {
        if (isFront && measuredSize.height > 0) {
            onFrontHeightChanged(measuredSize.height)
        }
    }

    /*
     * Tells the stack how far this koast has traveled toward leaving.
     * Called on every drag frame and every frame of the settle / dismiss
     * animations, so the cards behind follow the finger both ways.
     */
    fun publishSwipeProgress() {
        val width = measuredSize.width
            .toFloat()
            .coerceAtLeast(1f)

        val height = measuredSize.height
            .toFloat()
            .coerceAtLeast(1f)

        val horizontal = abs(dragX) / width
        val upward = (-dragY).coerceAtLeast(0f) / height

        currentOnSwipeProgress(
            max(horizontal, upward).coerceIn(0f, 1f)
        )
    }

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
                        publishSwipeProgress()
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = 0f,
                        spec = config.settleAnimationSpec
                    ) {
                        dragY = it
                        publishSwipeProgress()
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
                        publishSwipeProgress()
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = 0f,
                        spec = config.exitAnimationSpec
                    ) {
                        dragY = it
                        publishSwipeProgress()
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
                        publishSwipeProgress()
                    }
                }

                launch {
                    animateFloat(
                        from = startY,
                        to = -height * 1.15f,
                        spec = config.exitAnimationSpec
                    ) {
                        dragY = it
                        publishSwipeProgress()
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
            isFront &&
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

                        publishSwipeProgress()
                    }
                )
            }
        } else {
            Modifier
        }

    val clickableModifier =
        if (
            isFront &&
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

    val entersFromTop = config.position.isTop
    val enteredAtFront = remember { stackIndex == 0 }
    val slides = if (exiting) stackIndex == 0 else enteredAtFront

    val entranceY =
        if (!slides) 0f
        else (if (entersFromTop) -hiddenDistancePx else hiddenDistancePx) *
                (1f - visibilityProgress.value)

    val finalAlpha =
        (
                visibilityProgress.value *
                        gestureAlpha
                ).coerceIn(0f, 1f)

    val pull = if (stacked != null && !exiting && stackIndex > 0) frontSwipeProgress() else 0f

    val lastVisualIndex = remember { LastVisualIndex(stackIndex - pull) }

    val baseIndex = remember(stackIndex) { Animatable(lastVisualIndex.value + pull) }

    LaunchedEffect(baseIndex) {
        baseIndex.animateTo(
            targetValue = stackIndex.toFloat(),
            animationSpec = config.settleAnimationSpec
        )
    }

    val visualIndex = baseIndex.value - pull
    lastVisualIndex.value = visualIndex

    /*
     * Cards behind the front one shrink a little and peek out on the
     * side facing away from the screen edge.
     */
    val density = LocalDensity.current
    val stackDepth = visualIndex.coerceAtLeast(0f)

    val stackScale =
        if (stacked == null) 1f
        else (1f - stackDepth * stacked.scaleStep).coerceAtLeast(0f)

    val stackOffsetY = if (stacked == null) {
            0f
        } else {
            with(density) { stacked.peekOffset.toPx() } * stackDepth * (if (entersFromTop) 1f else -1f)
        }

    /*
     * One step back or more: the stack height. At the front: its own height.
     * In between: blended, so the card grows into its full size as it comes forward.
     */
    val forcedHeight: Dp? = run {
        val stackHeight = stackHeightPx()
        if (stacked == null || stackHeight <= 0f || stackDepth <= 0.001f) {
            return@run null
        }

        val natural =
            if (naturalHeightPx > 0) naturalHeightPx.toFloat()
            else stackHeight

        val expansion = (1f - stackDepth).coerceIn(0f, 1f)

        with(density) { lerp(stackHeight, natural, expansion).toDp() }
    }

    Box(
        modifier = modifier
            .then(
                if (config.compact) Modifier.widthIn(max = config.maxWidth)
                else Modifier.fillMaxWidth()
            )
            .graphicsLayer {
                transformOrigin = TransformOrigin(
                    pivotFractionX = 0.5f,
                    pivotFractionY = if (entersFromTop) 1f else 0f
                )
                scaleX = stackScale
                scaleY = stackScale
                translationX = dragX
                translationY = entranceY + dragY + stackOffsetY
                alpha = finalAlpha
            }
            .onSizeChanged {
                measuredSize = it
            }
            .then(gestureModifier)
            .then(clickableModifier)
    ) {
        Surface(
            modifier = Modifier
                .then(
                    if (config.compact) Modifier.widthIn(max = config.maxWidth)
                    else Modifier.fillMaxWidth()
                )
                .then(
                    if (forcedHeight != null) Modifier.height(forcedHeight)
                    else Modifier
                ),
            color = config.containerColor,
            contentColor = config.contentColor,
            shape = config.shape,
            shadowElevation = config.shadowElevation
        ) {
            Box(
                modifier = Modifier
                    .wrapContentHeight(unbounded = true)
                    .onSizeChanged {
                        naturalHeightPx = it.height
                    }
                    .then(
                        if (config.compact) Modifier.widthIn(max = config.maxWidth)
                        else Modifier.fillMaxWidth()
                    )
                    .heightIn(min = config.minHeight)
                    .padding(config.contentPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                if (koastContent != null) {
                    koastContent(koast)
                } else {
                    DefaultKoastContent(
                        koast = koast,
                        config = config,
                    )
                }
            }
        }
    }
}

@Composable
fun DefaultKoastContent(
    koast: KoastRequest,
    config: KoastyHostConfig,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

/*
 * Where a card was last drawn in the stack. A plain holder, not state,
 * so writing it during composition triggers nothing.
 */
private class LastVisualIndex(var value: Float)

/**
 * A koast the host is drawing. It stays drawn while it animates out,
 * after the manager has already removed it from the queue.
 */
@Stable
private class KoastEntry(val koast: KoastRequest) {
    var exiting by mutableStateOf(false)

    // Stack position the koast had when it started leaving.
    var exitIndex by mutableIntStateOf(0)
}

/*
 * How far the front koast has been swiped away: 0 = resting, 1 = fully gone.
 * Tagged with the id of the koast that reported it, so a leftover value from
 * a koast that has already left the queue is ignored.
 */
@Stable
private class SwipeProgressState {
    var ownerId by mutableStateOf<Any?>(null)
        private set

    var progress by mutableFloatStateOf(0f)
        private set

    fun report(id: Any?, value: Float) {
        ownerId = id
        progress = value
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
