package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class CameraState(
    imageCompressionMode: ImageCompressionMode = ImageCompressionMode.None,
    initialCameraMode: CameraMode
) {
    var cameraMode: CameraMode by mutableStateOf(initialCameraMode)
        private set
    var isCameraReady: Boolean by mutableStateOf(false)
        protected set

    var cameraCaptureOutputResult by mutableStateOf<CameraCaptureOutput?>(null)
        private set


    private val imageCaptureState = CameraCaptureState.Image(onCapture = {

        onCapture(it)
    }, imageCompressionMode = imageCompressionMode)
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

    internal var orientationListenerEnabled by mutableStateOf(false)

    @Composable
    fun ListenCaptureOutputResult(onCaptureOutputResult: (CameraCaptureOutput) -> Unit) {
        val result = cameraCaptureOutputResult
        LaunchedEffect(result) {
            if (result != null) {
                onCaptureOutputResult(result)
            }
            cameraCaptureOutputResult = null
        }
    }

    fun toggleCapture(mode: CameraCaptureMode) {
        if (!isCameraReady) return
        if (mode != cameraCaptureMode && captureState.isCapturing) {
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
            null -> CameraCaptureOutput.Error
            else -> CameraCaptureOutput.Success(cameraCaptureMode, outputFilePath)
        }
        cameraCaptureOutputResult = output
    }

    companion object {
        fun saver(): Saver<CameraState, Int> =
            Saver(
                save = {
                    it.cameraMode.id()
                },
                restore = {
                    CameraState(
                        initialCameraMode = cameraModeFromId(it),
                    )
                },
            )
    }
}

@Composable
fun rememberCameraState(
    initialCameraMode: CameraMode = CameraMode.Back,
): CameraState =
    rememberSaveable(
        saver = CameraState.saver(),
    ) {
        CameraState(
            initialCameraMode = initialCameraMode
        )
    }