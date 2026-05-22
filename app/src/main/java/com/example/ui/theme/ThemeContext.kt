package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Themecolors mirrors the React ThemeContext structure.
 * Dynamic light and dark mode colors are mapped precisely according to Phase 3.
 */
data class AppColors(
    val background: Color,
    val card: Color,
    val text: Color,
    val accent: Color,
    val isDark: Boolean
)

val LocalAppColors = compositionLocalOf {
    AppColors(
        background = Color(0xFF0F0F0F),
        card = Color(0xFF1A1A1A),
        text = Color(0xFFFFFFFF),
        accent = Color(0xFF6C63FF),
        isDark = true
    )
}
