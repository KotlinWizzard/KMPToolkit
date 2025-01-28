package com.kmptoolkit.cameraxgallery.camera.state


sealed class CameraCaptureOutput() {
    data object Error : CameraCaptureOutput()

    data class Success(val mode: CameraCaptureMode, val filePath: String) :
        CameraCaptureOutput()
}