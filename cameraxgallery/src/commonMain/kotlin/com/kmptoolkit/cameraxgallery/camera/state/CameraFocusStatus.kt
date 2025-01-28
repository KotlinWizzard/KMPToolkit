package com.kmptoolkit.cameraxgallery.camera.state

sealed class CameraFocusStatus {
    data object Idle : CameraFocusStatus()

    data class FocusRequested(
        val x: Float,
        val y: Float,
        val relativeXPercent: Float,
        val relativeYPercent: Float,
    ) : CameraFocusStatus()
}
