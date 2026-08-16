package com.deference.koasty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun KoastyProvider(
    koastManager: KoastManager,
    modifier: Modifier = Modifier,
    config: KoastyHostConfig = KoastyHostConfig(),
    leadingContent: (@Composable RowScope.(KoastRequest) -> Unit)? = null,
    koastContent: (@Composable (KoastRequest) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content()
        KoastyHost(
            koastManager = koastManager,
            modifier = Modifier
                .align(
                    when (config.position) {
                        KoastPosition.TOP_START -> Alignment.TopStart
                        KoastPosition.TOP_CENTER -> Alignment.TopCenter
                        KoastPosition.TOP_END -> Alignment.TopEnd
                        KoastPosition.BOTTOM_START -> Alignment.BottomStart
                        KoastPosition.BOTTOM_CENTER -> Alignment.BottomCenter
                        KoastPosition.BOTTOM_END -> Alignment.BottomEnd
                    }
                )
                .then(
                    if (config.compact) Modifier.padding(config.edgePadding)
                    else Modifier
                ),
            config = config,
            leadingContent = leadingContent,
            koastContent = koastContent
        )
    }
}
