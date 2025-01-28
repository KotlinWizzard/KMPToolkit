package com.kmptoolkit.core.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

sealed class ThemeStyle(open val colorScheme: ColorScheme) {
    data class Dark(override val colorScheme: ColorScheme):ThemeStyle(colorScheme)
    data class Light(override val colorScheme: ColorScheme):ThemeStyle(colorScheme)

    fun isDarkTheme() = this is Dark
    fun isLightTheme() = this is Light
}

class ThemeStyles(
    val dark: ThemeStyle.Dark,
    val light: ThemeStyle.Light,
    val current:ThemeStyle
)

val LocalThemeStyle = staticCompositionLocalOf<ThemeStyles> { error("No ThemeStyle provided") }
val MaterialTheme.themeStyle: ThemeStyles
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeStyle.current