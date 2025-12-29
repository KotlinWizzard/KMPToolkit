package io.github.kotlinwizzard.kmptoolkit.gallery

import androidx.compose.runtime.Composable

@Composable
expect internal fun launchMediaPicker(
    onResult: (List<Pair<ByteArray, MediaPickerMediaType>>) -> Unit,
    mediaPickerStatus: MediaPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
)