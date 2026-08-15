package com.deference.koasty.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
expect fun getColorScheme(
    isDark: Boolean,
    dynamicColor: Boolean = true
): ColorScheme

@Composable
fun KoastyDemoTheme(
    content: @Composable () -> Unit
) {
    val isDark: Boolean = isSystemInDarkTheme()

    val colorScheme = getColorScheme(
        isDark = isDark
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            Surface(content = content)
        }
    )
}