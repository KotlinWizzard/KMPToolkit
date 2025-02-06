package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.ui

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy

class ImageCaptureCallback(
    private val onCapture: (byteArray: ByteArray?) -> Unit,
    private val stopCapturing: () -> Unit,
) : ImageCapture.OnImageCapturedCallback() {
    override fun onCaptureSuccess(image: ImageProxy) {
        val imageBytes = image.toByteArray()
        onCapture(imageBytes)
        stopCapturing()
    }
}