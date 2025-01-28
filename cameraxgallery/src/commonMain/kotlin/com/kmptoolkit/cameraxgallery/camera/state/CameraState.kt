package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kmptoolkit.core.service.image.ImageCacheService

class CameraState(
    internal var onCapture: (CameraCaptureOutput) -> Unit,
    imageCacheService: ImageCacheService = ImageCacheService(),
    imageCompressionMode: ImageCompressionMode = ImageCompressionMode.None,
    initialCameraMode: CameraMode
) {
    var cameraMode: CameraMode by mutableStateOf(initialCameraMode)
        private set
    var isCameraReady: Boolean by mutableStateOf(false)
        protected set

    private val imageCaptureState = CameraCaptureState.Image(onCapture = {
        onCapture(it)
    }, imageCacheService = imageCacheService, imageCompressionMode = imageCompressionMode)
    private val videoCaptureState = CameraCaptureState.Video(onCapture = {
        onCapture(it)
    })
    var captureState: CameraCaptureState by mutableStateOf(imageCaptureState)
        private set
    val cameraCaptureMode by derivedStateOf {
        when (captureState) {
            is CameraCaptureState.Image -> CameraCaptureMode.Image
            is CameraCaptureState.Video -> CameraCaptureMode.Video
        }
    }
    val cameraFocusState: CameraFocusState = CameraFocusState()
    val cameraTorchState: CameraTorchState = CameraTorchState()

    fun toggleCameraMode() {
        cameraMode = cameraMode.inverse()
    }

    fun setImageCompression(imageCompressionMode: ImageCompressionMode) {
        imageCaptureState.imageCompressionMode = imageCompressionMode
    }

    fun toggleCapture(mode: CameraCaptureMode) {
        if (mode != cameraCaptureMode && captureState.isCapturing){
            return
        }
        val currentCaptureState = when (mode) {
            CameraCaptureMode.Image -> {
                imageCaptureState
            }
            CameraCaptureMode.Video -> videoCaptureState
        }
        captureState = currentCaptureState
        currentCaptureState.toggleCapture()
    }

    internal fun onCameraReady() {
        isCameraReady = true
    }

    private fun onCapture(outputFilePath: String?) {
        val output = when (outputFilePath) {
            null -> CameraCaptureOutput.Error(cameraCaptureMode)
            else -> CameraCaptureOutput.Success(cameraCaptureMode, outputFilePath)
        }
        onCapture.invoke(output)
    }

    companion object {
        fun saver(onCapture: (CameraCaptureOutput) -> Unit): Saver<CameraState, Int> =
            Saver(
                save = {
                    it.cameraMode.id()
                },
                restore = {
                    CameraState(
                        initialCameraMode = cameraModeFromId(it),
                        onCapture = onCapture,
                    )
                },
            )
    }
}

@Composable
fun rememberCameraState(
    initialCameraMode: CameraMode,
    imageCacheService: ImageCacheService = ImageCacheService(),
    onCapture: (CameraCaptureOutput) -> Unit,
): CameraState =
    rememberSaveable(
        saver = CameraState.saver(onCapture),
    ) {
        CameraState(
            onCapture = onCapture,
            imageCacheService = imageCacheService,
            initialCameraMode = initialCameraMode
        )
    }.apply {
        this.onCapture = onCapture
    }