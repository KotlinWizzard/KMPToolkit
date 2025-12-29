package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import io.github.kotlinwizzard.kmptoolkit.core.extensions.clickableWithoutRipple
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.mapFiles
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement


@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MediaDragAndDropContainer(
    modifier: Modifier,
    mediaPickerState: MediaPickerState,
    mediaPickerSelectionMode: MediaPickerSelectionMode,
    mediaPickerSelectionType: MediaPickerSelectionType,
    pickFilesOnClick: Boolean
) {
    val document = document
    val cache = LocalCache.current
    val coroutineScope = rememberCoroutineScope()
  
    WebElementView(modifier = modifier, factory = {
        val div = (document.createElement("div") as HTMLDivElement).apply {

        }
        div.addEventListener("click", { e ->
            e.preventDefault()
            if(pickFilesOnClick){
                mediaPickerState.launch(
                    mediaPickerMediaSelectionType = mediaPickerSelectionType,
                    mediaPickerSelectionMode = mediaPickerSelectionMode
                )
            }
        })
        div.addEventListener("dragover", { e ->
            (e as DragEvent).preventDefault()
        })
        div.addEventListener("drop", { e ->
            val ev = e as DragEvent
            ev.preventDefault()

            val dt = ev.dataTransfer ?: return@addEventListener
            val files = dt.files ?: return@addEventListener

            val filtered = buildList {
                for (i in 0 until files.length) {
                    val f = files.item(i) ?: continue
                    if (matchesSelectionType(f.type, mediaPickerSelectionType)) {
                        add(f)
                    }
                }
            }

            val limited = applySelectionMode(filtered, mediaPickerSelectionMode)

            coroutineScope.launch(Dispatchers.IO) {
                mediaPickerState.onResult(
                    limited.mapFiles(),
                    cache
                )
            }
        })
        div
    })
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
