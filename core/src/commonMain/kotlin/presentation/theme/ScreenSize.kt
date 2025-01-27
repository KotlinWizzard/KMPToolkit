package presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import extensions.toDp
import kotlinx.serialization.json.JsonNull.content

data class ScreenSize(
    val height: Dp,
    val width: Dp,
    val heightPx: Int,
    val widthPx: Int,
)

@Composable
private fun IntSize.toScreenSize() = ScreenSize(
    height = height.toDp(),
    width = width.toDp(),
    heightPx = height,
    widthPx = width,
)

val LocalScreenSize = staticCompositionLocalOf { ScreenSize(0.dp, 0.dp, 0, 0) }

@Composable
fun ScreenSizeProvider(content: @Composable () -> Unit) {
    val screenSize = getScreenSize().toScreenSize()
    CompositionLocalProvider(LocalScreenSize provides screenSize) {
        content()
    }
}

@Composable
internal expect fun getScreenSize(): IntSize