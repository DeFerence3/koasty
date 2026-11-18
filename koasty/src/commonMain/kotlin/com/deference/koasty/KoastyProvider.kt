package com.deference.koasty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KoastyProvider(
    koastManager: KoastManager,
    modifier: Modifier = Modifier,
    config: KoastyHostConfig = KoastyHostConfig(),
    koastContent: (@Composable (KoastRequest) -> Unit)? = null,
    stackHandle: (@Composable (KoastStackHandleState) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content()
        KoastyHost(
            koastManager = koastManager,
            modifier = Modifier.matchParentSize(),
            config = config,
            koastContent = koastContent,
            stackHandle = stackHandle
        )
    }
}
