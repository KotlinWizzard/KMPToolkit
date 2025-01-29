package com.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable
import com.kmptoolkit.cameraxgallery.camera.state.ImageCompressionMode

@Composable
expect internal fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
)