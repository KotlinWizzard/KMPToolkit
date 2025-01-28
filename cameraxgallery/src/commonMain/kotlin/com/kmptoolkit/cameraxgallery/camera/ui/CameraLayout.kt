package com.kmptoolkit.cameraxgallery.camera.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kmptoolkit.cameraxgallery.camera.state.CameraState

@Composable
fun CameraPreviewLayout(modifier: Modifier=Modifier,cameraState: CameraState){
    Box(modifier.cameraCaptureAnimation(cameraState)){
        CameraPreview(modifier = modifier, cameraState = cameraState)
        CameraFocusPreview(modifier = modifier, cameraState = cameraState)
    }
}