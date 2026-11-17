package com.deference.koasty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KoastyProvider(
    koastManager: KoastManager,
    modifier: Modifier = Modifier,
    config: KoastyHostConfig = KoastyHostConfig(),
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
                .align(config.position.toAlignment())
                .padding(config.edgePadding)
                .then(
                    if (config.respectSafeArea) Modifier.safeDrawingPadding()
                    else Modifier
                ),
            config = config,
            koastContent = koastContent
        )
    }
}
