package presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class IconSize(
    val extraSmall: Dp = 12.dp,
    val small: Dp = 16.dp,
    val medium: Dp = 24.dp,
    val large: Dp = 32.dp,
    val extraLarge: Dp = 48.dp,
)

val LocalIconSize = staticCompositionLocalOf<IconSize> { error("No IconSize provided") }
val MaterialTheme.iconSize: IconSize
    @Composable
    @ReadOnlyComposable
    get() = LocalIconSize.current