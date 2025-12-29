package io.github.kotlinwizzard.kmptoolkit.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.camera.state.CameraState

@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
)