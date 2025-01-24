package extensions

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun String.hexToColor(): Color? {
    val hex = this.removePrefix("#")
    if (hex.length != 6 && hex.length != 8) {
        return null
    }
    val red = hex.substring(0, 2).toInt(16)
    val green = hex.substring(2, 4).toInt(16)
    val blue = hex.substring(4, 6).toInt(16)
    val alpha = if (hex.length == 8) hex.substring(6, 8).toInt(16) else 255

    return Color(red, green, blue, alpha)
}


@Composable
fun Modifier.aspectRatio(
    width: Float,
    height: Float,
    matchHeighConstraintFirst: Boolean = false,
) = this.aspectRatio(width / height, matchHeighConstraintFirst)


@Composable
fun Modifier.maxWidthAlign(
    fraction: Float,
    alignment: Alignment.Horizontal = Alignment.Start,
) = this
    .wrapContentWidth(alignment)
    .fillMaxWidth(fraction)
    .wrapContentWidth(alignment)


@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit,
): T {
    val state by rememberUpdatedState(this)
    DisposableEffect(state) {
        val job =
            coroutineScope.launch {
                delay(delayMillis)
                onChange(state)
            }
        onDispose {
            job.cancel()
        }
    }
    return state
}

@Composable
fun <T> T.useDebounceWithUpdatedState(
    delayMillis: Long = 300L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit = {},
): T {
    val state by rememberUpdatedState(this)
    var updateState by remember { mutableStateOf(this) }
    
    DisposableEffect(state) {
        val job =
            coroutineScope.launch {
                delay(delayMillis)
                onChange(state)
                updateState = state
            }
        onDispose {
            job.cancel()
        }
    }
    return updateState
}