package com.deference.koasty

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class KoastPosition {
    TOP_START,
    TOP_CENTER,
    TOP_END,
    BOTTOM_START,
    BOTTOM_CENTER,
    BOTTOM_END,
}

private fun defaultKoastPosition(): KoastPosition = when (getPlatform().platform) {
    Platforms.DESKTOP, Platforms.WEB -> KoastPosition.BOTTOM_END
    Platforms.ANDROID, Platforms.IOS -> KoastPosition.TOP_CENTER
}

@Composable
fun TopKoast(
    visible: Boolean,
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    durationMillis: Long = 3000,
    position: KoastPosition = defaultKoastPosition(),
    onDismiss: () -> Unit,
) {
    val isTopPosition = position == KoastPosition.TOP_START ||
        position == KoastPosition.TOP_CENTER ||
        position == KoastPosition.TOP_END
    val useCompactLayout = position != KoastPosition.TOP_CENTER ||
        getPlatform().platform == Platforms.DESKTOP ||
        getPlatform().platform == Platforms.WEB
    val contentAlignment = when (position) {
        KoastPosition.TOP_START -> Alignment.TopStart
        KoastPosition.TOP_CENTER -> Alignment.TopCenter
        KoastPosition.TOP_END -> Alignment.TopEnd
        KoastPosition.BOTTOM_START -> Alignment.BottomStart
        KoastPosition.BOTTOM_CENTER -> Alignment.BottomCenter
        KoastPosition.BOTTOM_END -> Alignment.BottomEnd
    }

    LaunchedEffect(visible) {
        if (visible) {
            delay(durationMillis.milliseconds)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (useCompactLayout) Modifier.padding(16.dp) else Modifier),
        contentAlignment = contentAlignment
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { if (isTopPosition) -it else it }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { if (isTopPosition) -it else it }
            ) + fadeOut(),
        ) {
            Row(
                modifier = Modifier
                    .then(
                        if (useCompactLayout) Modifier.widthIn(max = 420.dp)
                        else Modifier.fillMaxWidth()
                    )
                    .background(backgroundColor)
                    .then(if (isTopPosition) Modifier.statusBarsPadding() else Modifier)
                    .padding(
                        horizontal = 24.dp,
                        vertical = 22.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(22.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = message,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
