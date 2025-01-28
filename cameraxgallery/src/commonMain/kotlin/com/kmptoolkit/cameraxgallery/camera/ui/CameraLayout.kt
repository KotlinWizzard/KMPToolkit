package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kmptoolkit.cameraxgallery.camera.state.CameraState
import com.kmptoolkit.core.presentation.theme.ToolkitTheme

@Composable
fun CameraPreviewLayout(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
    containerColor:Color = ToolkitTheme.colorScheme.background,
    bottomBar: @Composable BoxScope.() -> Unit = {}
) {
    Column(modifier = modifier.background(containerColor)) {
        Box(Modifier.weight(1F).fillMaxWidth().cameraCaptureAnimation(cameraState,containerColor)) {
            CameraPreview(modifier = Modifier.matchParentSize(), cameraState = cameraState)
            CameraFocusPreview(modifier = Modifier.matchParentSize(), cameraState = cameraState)
        }
        Box(modifier = Modifier.fillMaxWidth().background(containerColor)) {
            bottomBar(this)
        }
    }
}