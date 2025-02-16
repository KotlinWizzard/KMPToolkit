package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraCaptureState
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraFocusStatus
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraMode
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.CameraState
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.util.LifecycleEffect
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession.Companion.discoverySessionWithDeviceTypes
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDeviceInput.Companion.deviceInputWithDevice
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDuoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureTorchModeAuto
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.focusPointOfInterestSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.isTorchModeSupported
import platform.AVFoundation.position
import platform.AVFoundation.setTorchMode
import platform.AVFoundation.torchAvailable
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.kCMPixelFormat_32BGRA
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetDataSize
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.darwin.dispatch_queue_create
import platform.posix.memcpy
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit


private val deviceTypes =
    listOf(
        AVCaptureDeviceTypeBuiltInWideAngleCamera,
        AVCaptureDeviceTypeBuiltInDualWideCamera,
        AVCaptureDeviceTypeBuiltInUltraWideCamera,
        AVCaptureDeviceTypeBuiltInDualCamera,
        AVCaptureDeviceTypeBuiltInDuoCamera,
        AVCaptureDeviceTypeBuiltInTripleCamera,
    )

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    cameraState: CameraState,
) {
    val camera: AVCaptureDevice? =
        remember {
            discoverySessionWithDeviceTypes(
                deviceTypes = deviceTypes,
                mediaType = AVMediaTypeVideo,
                position =
                when (cameraState.cameraMode) {
                    CameraMode.Front -> AVCaptureDevicePositionFront
                    CameraMode.Back -> AVCaptureDevicePositionBack
                },
            ).devices
                .firstOrNull() as? AVCaptureDevice
        }

    println("=== camera: $camera")

    if (camera != null) {
        RealDeviceCamera(
            camera = camera,
            modifier = modifier,
            state = cameraState,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun RealDeviceCamera(
    state: CameraState,
    camera: AVCaptureDevice,
    modifier: Modifier,
) {
    val queue =
        remember {
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0UL)
        }
    val capturePhotoOutput = remember { AVCapturePhotoOutput() }
    val videoOutput = remember { AVCaptureVideoDataOutput() }
    val videoOutputFile = remember { AVCaptureMovieFileOutput() }

    val captureState = state.captureState


    val frameAnalyzerDelegate =
        remember {
            CameraFrameAnalyzerDelegate(state.captureState.onFrame)
        }




    when (val captureState = state.captureState) {
        is CameraCaptureState.Image -> {

            HandleTriggerImageCapture(
                cameraCaptureState = captureState,
                camera = camera, capturePhotoOutput = capturePhotoOutput
            )
        }

        is CameraCaptureState.Video -> {
            HandleTriggerVideoCapture(captureState, videoOutputFile)
        }

    }


    val captureSession: AVCaptureSession =
        remember {
            AVCaptureSession().also { captureSession ->
                captureSession.sessionPreset = AVCaptureSessionPresetPhoto
                val captureDeviceInput: AVCaptureDeviceInput =
                    deviceInputWithDevice(device = camera, error = null)!!
                captureSession.addInput(captureDeviceInput)
                captureSession.addOutput(capturePhotoOutput)
                captureSession.addOutput(videoOutputFile)

                if (captureSession.canAddOutput(videoOutputFile)) {
                    val captureQueue = dispatch_queue_create("sampleBufferQueue", attr = null)
                    videoOutput.setSampleBufferDelegate(frameAnalyzerDelegate, captureQueue)
                    videoOutput.alwaysDiscardsLateVideoFrames = true
                    videoOutput.videoSettings =
                        mapOf(
                            kCVPixelBufferPixelFormatTypeKey to kCMPixelFormat_32BGRA,
                        )
                    captureSession.addOutput(videoOutput)
                }
            }
        }

    val cameraPreviewLayer =
        remember {
            AVCaptureVideoPreviewLayer(session = captureSession)
        }

    // Update captureSession with new camera configuration whenever isFrontCamera changed.
    LaunchedEffect(state.cameraMode) {
        val dispatchGroup = dispatch_group_create()
        captureSession.beginConfiguration()
        captureSession.inputs.forEach { captureSession.removeInput(it as AVCaptureInput) }

        val newCamera =
            discoverySessionWithDeviceTypes(
                deviceTypes,
                AVMediaTypeVideo,
                if (state.cameraMode == CameraMode.Front) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack,
            ).devices.firstOrNull() as? AVCaptureDevice

        newCamera?.let {
            val newInput =
                AVCaptureDeviceInput.deviceInputWithDevice(it, error = null) as AVCaptureDeviceInput
            if (captureSession.canAddInput(newInput)) {
                captureSession.addInput(newInput)
            }
            state.cameraTorchState.setTorchAvailability(newCamera.torchAvailable)
        }

        captureSession.commitConfiguration()

        dispatch_group_enter(dispatchGroup)
        dispatch_async(queue) {
            captureSession.startRunning()
            dispatch_group_leave(dispatchGroup)
        }

        dispatch_group_notify(dispatchGroup, dispatch_get_main_queue()) {
            state.onCameraReady()
        }
    }

    LaunchedEffect(state.cameraFocusState.status) {
        if (camera.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus) &&
            camera.focusPointOfInterestSupported
        ) {
            if (state.cameraFocusState.status != CameraFocusStatus.Idle) {
                val point =
                    state.cameraFocusState.status as CameraFocusStatus.FocusRequested

                camera.lockForConfiguration(null) // Lock the camera for
                // configuration
                camera.focusPointOfInterest =
                    CGPointMake(
                        1 - point.relativeXPercent.toDouble(),
                        1 - point.relativeYPercent.toDouble(),
                    )
                camera.focusMode = AVCaptureFocusModeContinuousAutoFocus

                camera
                    .unlockForConfiguration() // Unlock after configuration
            }
        }
    }

    LifecycleEffect(onResume = {
        camera.lockForConfiguration(null)
        if (state.cameraTorchState.isTorchEnabled) {
            when {
                camera.isTorchModeSupported(AVCaptureTorchModeOn) -> camera.setTorchMode(
                    AVCaptureTorchModeOn
                )

                camera.isTorchModeSupported(AVCaptureTorchModeAuto) -> camera.setTorchMode(
                    AVCaptureTorchModeAuto
                )

                else -> state.cameraTorchState.setTorchAvailability(false)
            }
        } else {
            when {
                camera.isTorchModeSupported(AVCaptureTorchModeOff) -> camera.setTorchMode(
                    AVCaptureTorchModeOff
                )

                else -> state.cameraTorchState.setTorchAvailability(false)
            }
        }
        camera.unlockForConfiguration()
    }, onPause = {
        camera.lockForConfiguration(null)
        when {
            camera.isTorchModeSupported(AVCaptureTorchModeOff) -> camera.setTorchMode(
                AVCaptureTorchModeOff
            )

            else -> state.cameraTorchState.setTorchAvailability(false)
        }
        camera.unlockForConfiguration()
    })
    DisposableEffect(state.cameraTorchState.isTorchEnabled) {
        camera.lockForConfiguration(null)
        if (state.cameraTorchState.isTorchEnabled) {
            when {
                camera.isTorchModeSupported(AVCaptureTorchModeOn) -> camera.setTorchMode(
                    AVCaptureTorchModeOn
                )

                camera.isTorchModeSupported(AVCaptureTorchModeAuto) -> camera.setTorchMode(
                    AVCaptureTorchModeAuto
                )

                else -> state.cameraTorchState.setTorchAvailability(false)
            }
        } else {
            when {
                camera.isTorchModeSupported(AVCaptureTorchModeOff) -> camera.setTorchMode(
                    AVCaptureTorchModeOff
                )

                else -> state.cameraTorchState.setTorchAvailability(false)
            }
        }
        camera.unlockForConfiguration()
        onDispose {
        }
    }

    if (state.orientationListenerEnabled) {
        DisposableEffect(cameraPreviewLayer, capturePhotoOutput, videoOutput, state) {
            val listener = OrientationListener(cameraPreviewLayer, capturePhotoOutput, videoOutput)
            val notificationName = platform.UIKit.UIDeviceOrientationDidChangeNotification
            NSNotificationCenter.defaultCenter.addObserver(
                observer = listener,
                selector =
                NSSelectorFromString(
                    OrientationListener::orientationDidChange.name + ":",
                ),
                name = notificationName,
                `object` = null,
            )
            onDispose {
//                state.triggerCaptureAnchor = null
                NSNotificationCenter.defaultCenter.removeObserver(
                    observer = listener,
                    name = notificationName,
                    `object` = null,
                )
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        UIKitView(
            factory = {
                val dispatchGroup = dispatch_group_create()
                val cameraContainer = UIView()
                cameraContainer.backgroundColor = UIColor.blackColor // set background color
                cameraContainer.layer.addSublayer(cameraPreviewLayer)
                cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill

                dispatch_group_enter(dispatchGroup)
                dispatch_async(queue) {
                    captureSession.startRunning()
                    dispatch_group_leave(dispatchGroup)
                }
                dispatch_group_notify(dispatchGroup, dispatch_get_main_queue()) {
                    state.onCameraReady()
                }

                val rect =
                    CGRectMake(
                        0.0,
                        0.0,
                        maxWidth.value.toDouble(),
                        maxHeight.value.toDouble(),
                    )
                cameraPreviewLayer.setFrame(rect)
                cameraContainer
            },
            modifier = Modifier.matchParentSize(),
            properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
        )
    }
}

class PhotoCaptureDelegate(
    private val onCaptureEnd: () -> Unit,
    private val onCapture: (byteArray: ByteArray?) -> Unit,
) : NSObject(),
    AVCapturePhotoCaptureDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData != null) {
            var uiImage = UIImage(photoData)
            if (uiImage.imageOrientation != UIImageOrientation.UIImageOrientationUp) {
                UIGraphicsBeginImageContextWithOptions(
                    uiImage.size,
                    false,
                    uiImage.scale,
                )
                uiImage.drawInRect(
                    CGRectMake(
                        x = 0.0,
                        y = 0.0,
                        width = uiImage.size.useContents { width },
                        height = uiImage.size.useContents { height },
                    ),
                )
                val normalizedImage = UIGraphicsGetImageFromCurrentImageContext()
                UIGraphicsEndImageContext()
                uiImage = normalizedImage!!
            }
            val imageData = UIImagePNGRepresentation(uiImage)
            val byteArray: ByteArray? = imageData?.toByteArray()
            onCapture(byteArray)

        }
        onCaptureEnd()
    }
}

@OptIn(ExperimentalForeignApi::class)
private inline fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return byteArray
}

class OrientationListener(
    private val cameraPreviewLayer: AVCaptureVideoPreviewLayer,
    private val capturePhotoOutput: AVCapturePhotoOutput,
    private val videoOutput: AVCaptureVideoDataOutput,
) : NSObject() {
    @OptIn(BetaInteropApi::class)
    @Suppress("UNUSED_PARAMETER")
    @ObjCAction
    fun orientationDidChange(arg: NSNotification) {
        val cameraConnection = cameraPreviewLayer.connection
        val actualOrientation =
            when (UIDevice.currentDevice.orientation) {
                UIDeviceOrientation.UIDeviceOrientationPortrait ->
                    AVCaptureVideoOrientationPortrait

                UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ->
                    AVCaptureVideoOrientationLandscapeRight

                UIDeviceOrientation.UIDeviceOrientationLandscapeRight ->
                    AVCaptureVideoOrientationLandscapeLeft

                UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown ->
                    AVCaptureVideoOrientationPortrait

                else -> cameraConnection?.videoOrientation ?: AVCaptureVideoOrientationPortrait
            }
        if (cameraConnection != null) {
            cameraConnection.videoOrientation = actualOrientation
        }
        capturePhotoOutput
            .connectionWithMediaType(AVMediaTypeVideo)
            ?.videoOrientation = actualOrientation
        videoOutput
            .connectionWithMediaType(AVMediaTypeVideo)
            ?.videoOrientation = actualOrientation
    }
}

class CameraFrameAnalyzerDelegate(
    private val onFrame: ((frame: ByteArray) -> Unit)?,
) : NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCaptureOutput,
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        if (onFrame == null) return

        val imageBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return
        CVPixelBufferLockBaseAddress(imageBuffer, 0uL)
        val baseAddress = CVPixelBufferGetBaseAddress(imageBuffer)
        val bufferSize = CVPixelBufferGetDataSize(imageBuffer)
        val data = NSData.dataWithBytes(bytes = baseAddress, length = bufferSize)
        CVPixelBufferUnlockBaseAddress(imageBuffer, 0uL)

        val bytes = data.toByteArray()
        onFrame.invoke(bytes)
    }
}

@Composable
private fun HandleTriggerImageCapture(
    cameraCaptureState: CameraCaptureState.Image,
    camera: AVCaptureDevice,
    capturePhotoOutput: AVCapturePhotoOutput
) {
    val cache = LocalCache.current.imageCache
    val photoCaptureDelegate =
        remember(cameraCaptureState) {
            PhotoCaptureDelegate(cameraCaptureState::stopCapturing,
                { cameraCaptureState.onCapture(it, cache) })
        }

    val triggerCapture: () -> Unit = {
        val photoSettings =
            AVCapturePhotoSettings.photoSettingsWithFormat(
                format = mapOf(pair = AVVideoCodecKey to AVVideoCodecTypeJPEG),
            )
        if (camera.position == AVCaptureDevicePositionFront) {
            capturePhotoOutput
                .connectionWithMediaType(AVMediaTypeVideo)
                ?.automaticallyAdjustsVideoMirroring = false
            capturePhotoOutput
                .connectionWithMediaType(AVMediaTypeVideo)
                ?.videoMirrored = true
        }
        capturePhotoOutput.capturePhotoWithSettings(
            settings = photoSettings,
            delegate = photoCaptureDelegate,
        )
    }

    SideEffect {
        cameraCaptureState.triggerCaptureAnchor = triggerCapture
    }

    DisposableEffect(cameraCaptureState) {
        onDispose {
            cameraCaptureState.triggerCaptureAnchor = null
        }
    }
}

@Composable
fun HandleTriggerVideoCapture(
    cameraCaptureState: CameraCaptureState.Video,
    videoOutput: AVCaptureMovieFileOutput
) {
    val cache = LocalCache.current.videoCache

    var isRecording by remember { mutableStateOf(false) }
    var outputFilePath by remember { mutableStateOf<String?>(null) }
    var startTime by remember { mutableStateOf<Long?>(null) }

    val startRecording: () -> Unit = {
        val outputFile = cache.getFullPathFromFilename(cache.generateFilename())
        outputFilePath = outputFile
        val connection = videoOutput.connectionWithMediaType(AVMediaTypeVideo)

        connection?.videoOrientation = AVCaptureVideoOrientationPortrait
        if (videoOutput.isRecording()) {
            videoOutput.stopRecording()
        }

        videoOutput.startRecordingToOutputFileURL(
            NSURL.fileURLWithPath(outputFile),
            recordingDelegate = object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
                override fun captureOutput(
                    output: AVCaptureFileOutput,
                    didFinishRecordingToOutputFileAtURL: NSURL,
                    fromConnections: List<*>,
                    error: NSError?
                ) {
                    if (error != null) {
                        println("Capture error: ${error.localizedDescription}")
                        cameraCaptureState.onCapture(null)
                    } else {
                        println("Capture success")
                        cameraCaptureState.onCapture(outputFile)
                    }
                    cameraCaptureState.stopCapturing()
                }
            }
        )
        startTime = Clock.System.now().toEpochMilliseconds()
        isRecording = true
    }

    val stopRecording: () -> Unit = {
        if (videoOutput.isRecording()) {
            videoOutput.stopRecording()
        }
        isRecording = false
        startTime = null
    }

    // Update duration periodically
    LaunchedEffect(startTime) {
        while (startTime != null) {
            val elapsedNanos = Clock.System.now().toEpochMilliseconds() - (startTime ?: 0)
            cameraCaptureState.recordedDurationNanos = elapsedNanos
                .milliseconds.toLong(DurationUnit.NANOSECONDS)
            delay(1000L) // Update every second
        }
    }
    DisposableEffect(cameraCaptureState.isCapturing) {
        if (cameraCaptureState.isCapturing) {
            startRecording()
        } else {
            stopRecording()
        }
        onDispose {
            stopRecording()
        }
    }
}