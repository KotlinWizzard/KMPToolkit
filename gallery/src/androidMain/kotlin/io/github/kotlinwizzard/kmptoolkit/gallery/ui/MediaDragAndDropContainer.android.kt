package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState

@Composable
actual fun MediaDragAndDropContainer(
    modifier: Modifier,
    mediaPickerState: MediaPickerState,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerSelectionType: MediaPickerSelectionType,
    pickFilesOnClick: Boolean
) {
}