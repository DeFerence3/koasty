package com.deference.koasty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
                .align(Alignment.TopCenter),
            config = config,
            leadingContent = leadingContent,
            koastContent = koastContent
        )
    }
}