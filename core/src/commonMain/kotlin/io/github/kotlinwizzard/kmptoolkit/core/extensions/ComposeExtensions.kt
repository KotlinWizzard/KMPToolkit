package io.github.kotlinwizzard.kmptoolkit.core.extensions

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
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

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx().toInt() }

@Composable
fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

fun Color.toBrush(): Brush = SolidColor(this)


@Composable
fun Modifier.safeClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = androidx.compose.material3.ripple(),
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    minPointerDelayMillis: Long = 500L,
    onClick: () -> Unit,
): Modifier =
    composed {
        var enableAgain by remember { mutableStateOf(true) }

        LaunchedEffect(enableAgain) {
            if (!enableAgain) {
                delay(minPointerDelayMillis)
                enableAgain = true
            }
        }

        Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled && enableAgain,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            if (enableAgain) {
                enableAgain = false
                onClick()
            }
        }
    }

@Composable
fun Modifier.clickableWithRipple(
    onClick: () -> Unit,
    rippleColor: Color? = null,
    bounded: Boolean = true,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
): Modifier =
    composed {
        safeClickable(
            onClick = onClick,
            indication =
            if (rippleColor != null) {
                androidx.compose.material3.ripple(
                    bounded = bounded,
                    color = rippleColor
                )
            } else {
                androidx.compose.material3.ripple(
                    bounded = bounded,
                )
            },
            interactionSource = interactionSource,
            enabled = enabled,
        )
    }

@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit) =
    safeClickable(
        onClick = onClick,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
    )

val <T> ProvidableCompositionLocal<T?>.currentOrThrow: T
    @Composable
    get() = current ?: error("CompositionLocal is null")


@Composable
fun <T> rememberDerivedStateOf(calculation: () -> T) = remember {
    derivedStateOf(calculation)
}