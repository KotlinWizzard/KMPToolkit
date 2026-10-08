package io.github.kotlinwizzard.kmptoolkit.camera.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import io.github.kotlinwizzard.kmptoolkit.camera.state.CameraMode
import io.github.kotlinwizzard.kmptoolkit.camera.state.CameraState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

@Composable
internal fun loadCameraProvider(
    context: Context,
    cameraState: CameraState,
): State<ProcessCameraProvider?> =
    produceState<ProcessCameraProvider?>(null, context) {
        cameraState.onCameraInitializing()
        value = try {
            withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context.applicationContext).get()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // A device may advertise camera features even when no camera can actually be opened.
            // Keep the preview unavailable instead of crashing unrelated flows such as gallery use.
            Log.w(TAG, "Camera provider is unavailable", exception)
            cameraState.onCameraUnavailable()
            null
        }
    }

private const val TAG = "KMPToolkitCamera"

@Composable
internal fun getCameraSelector(state: CameraState) =
    remember(state.cameraMode) {
        val lensFacing =
            when (state.cameraMode) {
                CameraMode.Front -> {
                    CameraSelector.LENS_FACING_FRONT
                }

                CameraMode.Back -> {
                    CameraSelector.LENS_FACING_BACK
                }
            }
        CameraSelector.Builder().requireLensFacing(lensFacing).build()
    }

internal val aspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
internal val resolutionSelector =
    ResolutionSelector
        .Builder()
        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
        .setAspectRatioStrategy(
            aspectRatioStrategy,
        ).build()

@Composable
internal fun getImageAnalyzer(
    cameraState: CameraState,
    backgroundExecutor: Executor,
) = remember(cameraState.captureState.onFrame) {

    cameraState.captureState.onFrame.let { onFrame ->
        val analyzer =
            ImageAnalysis
                .Builder()
                .setResolutionSelector(
                    resolutionSelector,
                ).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

        analyzer.apply {
            setAnalyzer(backgroundExecutor) { imageProxy ->
                val imageBytes = imageProxy.toByteArray()
                cameraState.imageAnalyzers.forEach {
                    it.analyze(imageBytes)
                }
                if (onFrame != null) {
                    onFrame(imageBytes)
                }
            }
        }
    }
}

@Composable
internal fun ProcessCameraProvider.DisposeOnEffect() {
    DisposableEffect(Unit) {
        onDispose {
            unbindAll()
        }
    }
}
