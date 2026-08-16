package com.deference.koasty.app.theme

@androidx.compose.runtime.Composable
actual fun getColorScheme(
    isDark: Boolean,
    dynamicColor: Boolean,
): androidx.compose.material3.ColorScheme {
    return when {
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
}