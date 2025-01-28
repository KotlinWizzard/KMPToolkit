package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kmptoolkit.cameraxgallery.camera.state.CameraFocusStatus
import com.kmptoolkit.cameraxgallery.camera.state.CameraState
import com.kmptoolkit.core.extensions.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraFocusPreview(
    modifier: Modifier,
    cameraState: CameraState,
    size: Dp = 60.dp,
    animationDurationMillis: Long = 200L,
    maxFocusVisibilityTimeMillis: Long = 1500L,
    focusContent: @Composable BoxScope.(size: Dp) -> Unit = {
        Box(Modifier.matchParentSize().border(2.dp,  Color.White, CircleShape))
    }
) {
    var circleOffset by remember { mutableStateOf<Offset?>(null) }
    var isTapped by remember { mutableStateOf(false) }
    val circleSize by animateDpAsState(
        if (isTapped) size * 0.8F else size,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
    )
    DisposableEffect(circleOffset) {
        circleOffset?.let {
            cameraState.cameraFocusState.requestFocus(
                it.x,
                it.y,
            )
        }
        onDispose { }
    }

    val tapCoroutineScope = rememberCoroutineScope()
    DisposableEffect(isTapped) {
        val job =
            tapCoroutineScope.launch {
                delay(animationDurationMillis)
                if (isTapped) {
                    isTapped = false
                }
            }
        onDispose {
            job.cancel()
        }
    }

    val focusCoroutineScope = rememberCoroutineScope()
    DisposableEffect(cameraState.cameraFocusState.status) {
        val job =
            focusCoroutineScope.launch {
                delay(maxFocusVisibilityTimeMillis)
                if (cameraState.cameraFocusState.status is CameraFocusStatus.FocusRequested) {
                    cameraState.cameraFocusState.clearFocus()
                }
            }
        onDispose {
            job.cancel()
        }
    }

    BoxWithConstraints(
        modifier.pointerInput(Unit) {
            detectTapGestures {
                circleOffset = it
                isTapped = false
                isTapped = true
            }
        },
    ) {
        cameraState.cameraFocusState.updateCameraFocusSize(
            this.constraints.maxWidth,
            this.constraints.maxHeight,
        )
        val offset = circleOffset
        if (offset != null && cameraState.cameraFocusState.status is CameraFocusStatus.FocusRequested) {
            val circleCenterOffset by rememberUpdatedState(circleSize.toPx() / 2)
            Box(
                modifier =
                Modifier
                    .offset {
                        IntOffset(
                            offset.x.toInt() - circleCenterOffset,
                            offset.y.toInt() - circleCenterOffset,
                        )
                    }.size(circleSize),
                content = {
                    focusContent(this, circleSize)
                }
            )
        }
    }
}