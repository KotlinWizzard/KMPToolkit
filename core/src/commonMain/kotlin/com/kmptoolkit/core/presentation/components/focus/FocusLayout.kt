package com.kmptoolkit.core.presentation.components.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import extensions.currentOrThrow
import extensions.toPx

@Composable
fun FocusLayout(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalParentFocusState provides FocusLayoutState()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .closeKeyboardOnTouch(LocalParentFocusState.currentOrThrow),
        ) { content() }
    }
}

@Composable
private fun imePaddingValues(): PaddingValues {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val paddingValues by rememberUpdatedState(
        PaddingValues(
            start = with(density) { imeInsets.getLeft(density, LayoutDirection.Ltr).toDp() },
            top = with(density) { imeInsets.getTop(density).toDp() },
            end = with(density) { imeInsets.getRight(density, LayoutDirection.Ltr).toDp() },
            bottom = with(density) { imeInsets.getBottom(density).toDp() },
        ),
    )

    return paddingValues
}

@Composable
private fun Modifier.closeKeyboardOnTouch(focusState: FocusLayoutState) =
    composed {
        val imePadding = imePaddingValues()
        var size by remember { mutableStateOf(Size.Zero) }
        val allowedSize by rememberUpdatedState(
            size
                .copy(
                    height = size.height - imePadding.calculateBottomPadding().toPx(),
                ).toRect(),
        )
        focusRequester(focusState.focusRequester)
            .focusable()
            .onSizeChanged { size = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(size) {
                detectTapGestures {
                    if (it in allowedSize) {
                        focusState.hideKeyboard()
                    }
                }
            }
    }

