package com.kmptoolkit.cameraxgallery.camera.state


sealed class CameraCaptureOutput(open val mode: CameraCaptureMode) {
    data class Error(override val mode: CameraCaptureMode) : CameraCaptureOutput(mode)

    data class Success(override val mode: CameraCaptureMode, val filePath: String) :
        CameraCaptureOutput(mode) {
    }
}