package com.deference.koasty.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getColorScheme(
    isDark: Boolean,
    dynamicColor: Boolean,
): ColorScheme {
    return when {
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
}