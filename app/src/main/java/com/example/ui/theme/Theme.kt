package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkAttendEaseColorScheme = darkColorScheme(
    primary = BrightAccent,
    secondary = LighterAccent,
    tertiary = DarkerAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = GrayText
)

private val LightAttendEaseColorScheme = lightColorScheme(
    primary = BrightAccent,
    secondary = LighterAccent,
    tertiary = DarkerAccent,
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF111111),
    onTertiary = Color(0xFF111111),
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF606060)
)

@Composable
fun MyApplicationTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val appColors = if (isDark) {
        AppColors(
            background = Color(0xFF0F0F0F),
            card = Color(0xFF1A1A1A),
            text = Color(0xFFFFFFFF),
            accent = Color(0xFF6C63FF),
            isDark = true
        )
    } else {
        AppColors(
            background = Color(0xFFF5F5F5),
            card = Color(0xFFFFFFFF),
            text = Color(0xFF111111),
            accent = Color(0xFF6C63FF),
            isDark = false
        )
    }

    val scheme = if (isDark) DarkAttendEaseColorScheme else LightAttendEaseColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
