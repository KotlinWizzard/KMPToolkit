package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kmptoolkit.cameraxgallery.camera.state.CameraState

@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
)