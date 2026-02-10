package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.WebElementView
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import io.github.kotlinwizzard.kmptoolkit.core.extensions.clickableWithoutRipple
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.chooseFile
import io.github.kotlinwizzard.kmptoolkit.gallery.mapFiles
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.mapFilesPdf
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.files.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun MediaImageOrPdfDragAndDropContainer(
    modifier: Modifier,
    mediaPickerState: MediaPickerState,
    pdfPickerState: PdfPickerState,
    maxImages: Int,
    pickFilesOnClick: Boolean
) {
    val document = document
    val cache = LocalCache.current

    DragAndDropContainer(
        modifier = modifier,
        pickFilesOnClick = pickFilesOnClick,
        onClick = { coroutineScope ->
            coroutineScope.launch {
                chooseFile(document, onResult = { files ->
                    handleFiles(
                        files = files,
                        coroutineScope = coroutineScope,
                        mediaPickerState = mediaPickerState,
                        pdfPickerState = pdfPickerState,
                        mediaCache = cache
                    )
                }, multiple = maxImages > 1, extensions = extensions)
            }
        },
        onDrop = { coroutineScope, files ->
            handleFiles(
                files,
                coroutineScope,
                mediaPickerState,
                pdfPickerState,
                mediaCache = cache
            )
        }
    )
}

private fun handleFiles(
    files: List<File>,
    coroutineScope: CoroutineScope,
    mediaPickerState: MediaPickerState,
    pdfPickerState: PdfPickerState,
    mediaCache: MediaCache,
) {
    coroutineScope.launch(Dispatchers.IO) {
        val imageFiles = files.filter { it.type.startsWith("image/") }
        val pdfFiles = files.filter { it.type.startsWith("application/pdf") }
        mediaPickerState.onResult(
            imageFiles.mapFiles(),
            mediaCache
        )
        pdfPickerState.onResult(
            pdfFiles.mapFilesPdf(
                mediaCache.imageCache,
                mediaCache.pdf
            ),
        )

    }
}


private val extensions = listOf(
    "jpg",
    "jpeg",
    "png",
    "webp"
) + listOf("pdf")

