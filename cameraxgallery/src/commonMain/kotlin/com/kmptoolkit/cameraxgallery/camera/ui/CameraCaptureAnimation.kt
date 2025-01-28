package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import com.kmptoolkit.cameraxgallery.camera.state.CameraCaptureMode
import com.kmptoolkit.cameraxgallery.camera.state.CameraState

@Composable
fun Modifier.cameraCaptureAnimation(
    cameraState: CameraState,
    backgroundColor: Color = Color.Black
): Modifier {
    val captureStarted = remember { mutableStateOf(false) }
    when (cameraState.cameraCaptureMode) {
        CameraCaptureMode.Image -> Unit
        CameraCaptureMode.Video -> return this
    }
    LaunchedEffect(cameraState.captureState.isCapturing) {
        if (cameraState.captureState.isCapturing) {
            captureStarted.value = true
        }
    }
    val visibility by animateFloatAsState(
        if (captureStarted.value) 1F else 0F,
        animationSpec =
        tween(
            300,
        ),
        finishedListener = {
            if (it == 1F) {
                captureStarted.value = false
            }
        },
    )
    return this
        .drawWithContent {
            drawContent()
            drawRect(backgroundColor, alpha = visibility)
        }
}