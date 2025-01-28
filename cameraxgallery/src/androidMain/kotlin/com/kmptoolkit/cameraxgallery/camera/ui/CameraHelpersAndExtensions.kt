package com.kmptoolkit.cameraxgallery.camera.ui

import android.content.Context
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
import com.kmptoolkit.cameraxgallery.camera.state.CameraMode
import com.kmptoolkit.cameraxgallery.camera.state.CameraState
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
internal fun loadCameraProvider(context: Context): State<ProcessCameraProvider?> =
    produceState<ProcessCameraProvider?>(null, context) {
        value =
            withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                ProcessCameraProvider.getInstance(context).get()
            }
    }

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

    cameraState.captureState.onFrame?.let { onFrame ->
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
                onFrame(imageBytes)
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
