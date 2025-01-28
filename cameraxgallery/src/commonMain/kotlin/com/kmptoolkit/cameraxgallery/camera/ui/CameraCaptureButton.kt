package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kmptoolkit.cameraxgallery.camera.state.CameraCaptureMode
import com.kmptoolkit.cameraxgallery.camera.state.CameraCaptureState
import com.kmptoolkit.cameraxgallery.camera.state.CameraState


enum class CameraCaptureButtonMode {
    Image,
    Video,
    Combined
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureButton(
    color: Color = Color.White,
    size: Dp = 80.dp,
    cameraState: CameraState,
    videoColor: Color = Color.Red,
    cameraCaptureButtonMode: CameraCaptureButtonMode = CameraCaptureButtonMode.Combined
) {
    val border = 4.dp
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState()

    val scaleFactor =
        animateFloatAsState(
            targetValue = if (pressed.value) 1.0F else 0.8F,
            animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )

    LaunchedEffect(pressed.value) {
        if (!pressed.value && cameraState.cameraCaptureMode == CameraCaptureMode.Video) {
            cameraState.toggleCapture(CameraCaptureMode.Video)
        }
    }

    val isVideoCapturing by rememberUpdatedState(cameraState.captureState.isCapturing && cameraState.captureState is CameraCaptureState.Video)
    val rippleAlpha = if (isVideoCapturing) 0.3F else 0.6F

    CompositionLocalProvider(LocalRippleConfiguration provides captureRipple(color)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
            Modifier
                .size(size),
        ) {
            Box(
                modifier =
                Modifier
                    .size(size * scaleFactor.value)
                    .clip(CircleShape)
                    .border(border, color, CircleShape)
                    .indication(
                        interactionSource, androidx.compose.material3.ripple(
                            bounded = true,
                            color = color.copy(rippleAlpha)
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            if (cameraCaptureButtonMode == CameraCaptureButtonMode.Combined) {
                                cameraState.toggleCapture(
                                    CameraCaptureMode.Video
                                )
                            }

                        }, onTap = {
                            if (cameraCaptureButtonMode == CameraCaptureButtonMode.Combined) {
                                cameraState.toggleCapture(
                                    CameraCaptureMode.Image
                                )
                            }

                        }, onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            when (cameraCaptureButtonMode) {
                                CameraCaptureButtonMode.Video -> cameraState.toggleCapture(
                                    CameraCaptureMode.Video
                                )

                                else -> Unit
                            }
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                            println("TEST_GESTURE release")
                            when (cameraCaptureButtonMode) {
                                CameraCaptureButtonMode.Image -> cameraState.toggleCapture(
                                    CameraCaptureMode.Image
                                )

                                CameraCaptureButtonMode.Video -> cameraState.toggleCapture(
                                    CameraCaptureMode.Video
                                )

                                else -> Unit
                            }
                        })
                    },
            ) {
                AnimatedVisibility(
                    isVideoCapturing,
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(0.6F)
                ) {
                    Box(
                        Modifier.matchParentSize().background(
                            videoColor,
                            CircleShape,
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun captureRipple(color: Color) =
    RippleConfiguration(
        color = color,
        rippleAlpha = RippleAlpha(1f, 1f, 1f, 1f),
    )

