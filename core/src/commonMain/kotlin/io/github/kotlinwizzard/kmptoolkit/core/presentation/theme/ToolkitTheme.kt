package io.github.kotlinwizzard.kmptoolkit.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.kotlinwizzard.kmptoolkit.core.presentation.components.focus.FocusLayout


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
        LocalThemeStyle provides ThemeStyles(
            dark = ThemeStyle.Dark(darkColorScheme.invoke()),
            light = ThemeStyle.Light(lightColorScheme.invoke()),
            current = if (darkTheme) ThemeStyle.Dark(darkColorScheme.invoke()) else ThemeStyle.Light(
                lightColorScheme.invoke()
            )
        ),
    ) {
        ScreenSizeProvider {

            val colorScheme = LocalThemeStyle.current.current.colorScheme
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

@Composable
fun ToolkitDarkScreen(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LocalThemeStyle.current.dark.colorScheme,
        shapes = ToolkitTheme.shapes,
        content=content
    )
}

@Composable
fun ToolkitLightScreen(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LocalThemeStyle.current.dark.colorScheme,
        shapes = ToolkitTheme.shapes,
        content=content
    )
}