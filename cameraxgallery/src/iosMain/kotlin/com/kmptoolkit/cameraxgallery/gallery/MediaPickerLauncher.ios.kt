package com.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable

@Composable
internal actual fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
}