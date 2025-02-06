package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize

@Composable
fun ToolkitOverlayLayout(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    overlay: @Composable BoxScope.() -> Unit
) {
    var size by remember { mutableStateOf(DpSize.Zero) }
    val density = LocalDensity.current
    Box(Modifier) {
        Box(modifier.onGloballyPositioned {
            with(density) {
                size = DpSize(it.size.width.toDp(), it.size.height.toDp())
            }
        }) {
            content(this)
        }

        Box(Modifier.size(size)) {
            overlay(this)
        }
    }
}