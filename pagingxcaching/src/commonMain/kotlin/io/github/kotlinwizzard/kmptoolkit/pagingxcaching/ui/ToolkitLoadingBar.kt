package io.github.kotlinwizzard.kmptoolkit.pagingxcaching.ui

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kotlinwizzard.kmptoolkit.core.presentation.theme.ToolkitTheme

@Composable
fun ToolkitLoadingBar(
    modifier: Modifier = Modifier,
    toolkitLoadingBarDefaults: ToolkitLoadingBarDefaults = ToolkitLoadingBarDefaults(),
) {
    CircularProgressIndicator(
        modifier,
        color = toolkitLoadingBarDefaults.color(),
        trackColor = toolkitLoadingBarDefaults.trackColor(),
        strokeWidth = toolkitLoadingBarDefaults.strokeWidth
    )
}

data class ToolkitLoadingBarDefaults(
    val color: @Composable () -> Color = { ToolkitTheme.colorScheme.primary },
    val trackColor: @Composable () -> Color = { color().copy(alpha = 0.6F) },
    val strokeWidth: Dp = TrackThickness
) {
    companion object {
        val TrackThickness = 4.dp
    }
}