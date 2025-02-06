package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.Composable
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state.ImageCompressionMode

@Composable
expect internal fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
)