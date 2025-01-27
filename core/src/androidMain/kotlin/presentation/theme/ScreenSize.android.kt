package presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
actual fun getScreenSize(): IntSize {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(config, density) {
        val width =
            with(density) {
                config.screenWidthDp.dp.toPx()
            }.roundToInt()
        val height =
            with(density) {
                config.screenHeightDp.dp.toPx()
            }.roundToInt()
        IntSize(
            width,
            height,
        )
    }
}