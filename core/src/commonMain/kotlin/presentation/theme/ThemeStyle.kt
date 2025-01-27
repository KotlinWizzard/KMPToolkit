package presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeStyle {
    Dark,
    Light;

    fun isDarkTheme() = this == Dark
    fun isLightTheme() = this == Light
}

val LocalThemeStyle = staticCompositionLocalOf<ThemeStyle> { error("No ThemeStyle provided") }
val MaterialTheme.themeStyle: ThemeStyle
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeStyle.current