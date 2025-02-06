package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.ui

import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.Consumer
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraCaptureState
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraFocusStatus
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraState
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import java.util.concurrent.Executors

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraState: CameraState,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProvider: ProcessCameraProvider? by loadCameraProvider(context)
    val preview =
        Preview
            .Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
    val previewView = remember { PreviewView(context) }

    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val videoRecorder = remember { Recorder.Builder().build() }
    val videoCapture = remember { VideoCapture.withOutput(videoRecorder) }
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageAnalyzer = getImageAnalyzer(cameraState, backgroundExecutor)
    val cameraSelector = getCameraSelector(cameraState)
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    cameraProvider?.DisposeOnEffect()
    LaunchedEffect(cameraState.cameraMode, cameraProvider, imageAnalyzer) {
        if (cameraProvider != null) {
            cameraState.onCameraReady()
            cameraProvider?.unbindAll()
            cameraProvider
                ?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *listOfNotNull(
                        preview,
                        imageCapture,
                        imageAnalyzer,
                        videoCapture
                    ).toTypedArray(),
                ).apply {
                    cameraState.cameraTorchState.setTorchAvailability(
                        this?.cameraInfo?.hasFlashUnit() ?: false,
                    )
                    cameraControl = this?.cameraControl
                }
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    }

    ListenTorchState(cameraControl, cameraState)
    ListenFocusState(cameraControl, cameraState, previewView)

    when (val cameraCaptureState = cameraState.captureState) {
        is CameraCaptureState.Image -> {
            HandleTriggerImageCapture(
                cameraCaptureState = cameraCaptureState,
                imageCapture = imageCapture
            )
        }

        is CameraCaptureState.Video -> {
            HandleTriggerVideoCapture(
                cameraCaptureState = cameraCaptureState,
                videoCapture = videoCapture
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier,
    ) {
        val resolution = preview.resolutionInfo?.resolution

        val aspectRatio =
            if (resolution != null
            ) {
                resolution.width.toFloat() / resolution.height
            } else {
                16f / 9f
            }

        val width = maxWidth
        val height = (width / aspectRatio).coerceAtMost(maxHeight)

        AndroidView(
            factory = { previewView },
            modifier =
            Modifier
                .width(width)
                .height(height)
                .align(Alignment.Center),
        )
    }
}

@Composable
private fun HandleTriggerImageCapture(
    cameraCaptureState: CameraCaptureState.Image,
    imageCapture: ImageCapture
) {
    val cache = LocalCache.current
    DisposableEffect(cameraCaptureState.triggerCaptureAnchor) {
        val triggerCapture = {
            imageCapture.takePicture(
                executor,
                ImageCaptureCallback(
                    {
                        cameraCaptureState.onCapture(it, cache.imageCache)
                    },
                    cameraCaptureState::stopCapturing
                ),
            )
        }
        cameraCaptureState.triggerCaptureAnchor = triggerCapture
        onDispose { cameraCaptureState.triggerCaptureAnchor = null }
    }

}

@Composable
private fun HandleTriggerVideoCapture(
    cameraCaptureState: CameraCaptureState.Video,
    videoCapture: VideoCapture<Recorder>
) {
    val context = LocalContext.current
    var activeRecording: Recording? by remember { mutableStateOf(null) }
    val videoRecordingListener = remember {
        Consumer<VideoRecordEvent> { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    cameraCaptureState.isCaptureDisable = true
                }
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        cameraCaptureState.onCapture(null)
                        if (cameraCaptureState.isCapturing) {
                            cameraCaptureState.stopCapturing()
                        }

                    } else {
                        cameraCaptureState.onCapture(event.outputResults.outputUri.path)
                    }
                    cameraCaptureState.isCaptureDisable = false
                }

                is VideoRecordEvent.Pause -> Unit
                is VideoRecordEvent.Status -> {
                    cameraCaptureState.recordedDurationNanos =
                        event.recordingStats.recordedDurationNanos
                }
            }
        }
    }
    val cache = LocalCache.current.videoCache
    LaunchedEffect(cameraCaptureState.isCapturing) {
        if (cameraCaptureState.isCapturing) {
            activeRecording = startRecording(
                cache.getFullPathFromFilename(cache.generateFilename()),
                videoCapture,
                context,
                videoRecordingListener
            )
        } else {
            activeRecording?.stop()
            activeRecording = null
        }
    }

    DisposableEffect(cameraCaptureState){
        onDispose {
            activeRecording?.stop()
            activeRecording = null
            cameraCaptureState.stopCapturing()
            cameraCaptureState.isCaptureDisable = false
        }
    }
}

@Composable
private fun ListenTorchState(cameraControl: CameraControl?, cameraState: CameraState) {
    LaunchedEffect(cameraControl, cameraState.cameraTorchState.isTorchEnabled) {
        cameraControl?.enableTorch(cameraState.cameraTorchState.isTorchEnabled)
    }
}

@Composable
private fun ListenFocusState(
    cameraControl: CameraControl?,
    cameraState: CameraState,
    previewView: PreviewView
) {
    LaunchedEffect(cameraControl, cameraState.cameraFocusState.status) {
        cameraControl?.let { control ->
            cameraState.cameraFocusState.status.let { focusStatus ->
                if (focusStatus is CameraFocusStatus.FocusRequested) {
                    val meteringPoint =
                        previewView.meteringPointFactory.createPoint(focusStatus.x, focusStatus.y)
                    val meteringAction =
                        FocusMeteringAction
                            .Builder(meteringPoint)
                            .build()
                    control.cancelFocusAndMetering()
                    control.startFocusAndMetering(meteringAction)
                }
            }
        }
    }

}


private val executor = Executors.newSingleThreadExecutor()

