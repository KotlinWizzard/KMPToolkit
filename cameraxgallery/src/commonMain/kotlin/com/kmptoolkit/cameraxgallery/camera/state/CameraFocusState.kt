package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize

class CameraFocusState {
    var status by mutableStateOf<CameraFocusStatus>(CameraFocusStatus.Idle)
        private set

    var cameraFocusSize by mutableStateOf<IntSize>(IntSize.Zero)
        private set

    var enabled by mutableStateOf(true)


    fun updateCameraFocusSize(
        width: Int,
        height: Int,
    ) {
        cameraFocusSize = IntSize(width, height)
    }

    fun requestFocus(
        x: Float,
        y: Float,
    ) {
        if (!enabled) return
        status =
            CameraFocusStatus.FocusRequested(
                x,
                y,
                relativeXPercent = (x / cameraFocusSize.width).coerceIn(0F, 1F),
                relativeYPercent = (y / cameraFocusSize.height).coerceIn(0F, 1F),
            )
    }

    fun clearFocus() {
        if (!enabled) return
        status = CameraFocusStatus.Idle
    }
}