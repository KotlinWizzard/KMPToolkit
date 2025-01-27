package presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import presentation.components.focus.FocusLayout

val ToolkitTheme: MaterialTheme
    get() = MaterialTheme


@Composable
fun ToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacing: Spacing = Spacing(),
    elevation: Elevation = Elevation(),
    iconSize: IconSize = IconSize(),
    shapes: Shapes = MaterialTheme.shapes,
    lightColorScheme: @Composable () -> ColorScheme,
    darkColorScheme: @Composable () -> ColorScheme,
    content: @Composable () -> Unit,
) {

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalElevation provides elevation,
        LocalIconSize provides iconSize,
        LocalThemeStyle provides if (darkTheme) ThemeStyle.Dark else ThemeStyle.Light,
    ) {
        ScreenSizeProvider {

            val colorScheme =
                when {
                    darkTheme -> darkColorScheme
                    else -> lightColorScheme
                }.invoke()
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography(),
                content = {
                    FocusLayout(content)
                },
                shapes = shapes,
            )
        }
    }
}