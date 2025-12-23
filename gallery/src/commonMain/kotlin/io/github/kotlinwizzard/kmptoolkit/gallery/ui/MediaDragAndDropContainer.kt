package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState

@Composable
// Web-only API:
// This composable is fully supported **only on Web targets** (JS & WASM).
// On all other targets (e.g. Android, iOS, Desktop/JVM) this is provided
// as a stub implementation without drag & drop functionality.
expect fun MediaDragAndDropContainer(
    modifier: Modifier = Modifier,
    mediaPickerState: MediaPickerState,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerSelectionType: MediaPickerSelectionType,
    pickFilesOnClick: Boolean = false

)


@Composable
// Web-only API:
// This composable is fully supported **only on Web targets** (JS & WASM).
// On all other targets (e.g. Android, iOS, Desktop/JVM) this is provided
// as a stub implementation without drag & drop functionality.
fun MediaDragAndDropLayout(
    modifier: Modifier = Modifier,
    mediaPickerState: MediaPickerState,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerSelectionType: MediaPickerSelectionType,
    contentAlignment: Alignment = Alignment.TopStart,
    pickFilesOnClick: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        MediaDragAndDropContainer(
            modifier = Modifier.matchParentSize(),
            mediaPickerState = mediaPickerState,
            mediaPickerSelectionMode = mediaPickerSelectionMode,
            mediaPickerSelectionType = mediaPickerSelectionType,
            pickFilesOnClick = pickFilesOnClick
        )
        content()
    }
}