package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable

@Composable
internal actual fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    when(mediaPickerStatus.mediaPickerSelectionMode){
        is MediaPickerSelectionMode.Multiple -> pickMultipleImages(
            selectionMode = mediaPickerStatus.mediaPickerSelectionMode,
            mediaPickerMediaSelectionType = mediaPickerStatus.mediaPickerMediaSelectionType,
            onResult = onResult,
            mediaPickerLauncherState = mediaPickerLauncherState
        )
        is MediaPickerSelectionMode.Single -> pickSingleImages(
            selectionMode = mediaPickerStatus.mediaPickerSelectionMode,
            mediaPickerMediaSelectionType = mediaPickerStatus.mediaPickerMediaSelectionType,
            onResult = onResult,
            mediaPickerLauncherState = mediaPickerLauncherState
        )
    }
}