package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.mapFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MediaDragAndDropContainer(
    modifier: Modifier,
    mediaPickerState: MediaPickerState,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerSelectionType: MediaPickerSelectionType,
    pickFilesOnClick: Boolean
) {
    val cache = LocalCache.current


    DragAndDropContainer(
        modifier = modifier,
        pickFilesOnClick = pickFilesOnClick,
        onClick = { _ ->
            mediaPickerState.launch(
                mediaPickerMediaSelectionType = mediaPickerSelectionType,
                mediaPickerSelectionMode = mediaPickerSelectionMode
            )
        },
        onDrop = { coroutineScope, files ->
            val filtered = files.filter { matchesSelectionType(it.type, mediaPickerSelectionType) }
            val limited = applySelectionMode(filtered, mediaPickerSelectionMode)
            coroutineScope.launch(Dispatchers.IO) {
                mediaPickerState.onResult(
                    limited.mapFiles(),
                    cache
                )
            }
        }
    )
}


private fun matchesSelectionType(mime: String, type: MediaPickerSelectionType): Boolean {
    if (mime.isBlank()) return false

    return when (type) {
        MediaPickerSelectionType.Image -> mime.startsWith("image/")
        MediaPickerSelectionType.Video -> mime.startsWith("video/")
        MediaPickerSelectionType.ImageAndVideo -> mime.startsWith("image/") || mime.startsWith("video/")
    }
}

private fun applySelectionMode(
    files: List<org.w3c.files.File>,
    mode: MediaPickerSelectionMode
): List<org.w3c.files.File> {
    return when (mode) {
        is MediaPickerSelectionMode.Single -> files.take(1)
        is MediaPickerSelectionMode.Multiple -> {
            val max = mode.maxSelection
            if (max == MediaPickerSelectionMode.INFINITY) files else files.take(max.coerceAtLeast(0))
        }
    }
}