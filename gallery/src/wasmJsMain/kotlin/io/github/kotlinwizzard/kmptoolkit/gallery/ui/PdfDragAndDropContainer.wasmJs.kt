package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import io.github.kotlinwizzard.kmptoolkit.core.extensions.clickableWithoutRipple
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.initPdfJs
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.mapFilesPdf
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLDivElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun PdfDragAndDropContainer(
    modifier: Modifier,
    pdfPickerState: PdfPickerState,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    pickFilesOnClick: Boolean
) {
    val cache = LocalCache.current
    DragAndDropContainer(
        modifier = modifier,
        pickFilesOnClick = pickFilesOnClick,
        onClick = { _ ->
            pdfPickerState.launch(
                selectionMode = pdfPickerSelectionMode
            )
        },
        onDrop = { coroutineScope, files ->
            val filtered = files.filter { matchesSelectionType(it.type) }
            val limited = applySelectionMode(filtered, pdfPickerSelectionMode)
            coroutineScope.launch(Dispatchers.IO) {
                pdfPickerState.onResult(
                    limited.mapFilesPdf(
                        imageCache = cache.imageCache,
                        pdfCacheService = cache.pdf
                    )
                )
            }
        }
    )
}


private fun matchesSelectionType(mime: String): Boolean {
    if (mime.isBlank()) return false
    return mime.startsWith("application/pdf")
}

private fun applySelectionMode(
    files: List<org.w3c.files.File>,
    mode: PdfPickerSelectionMode
): List<org.w3c.files.File> {
    return when (mode) {
        PdfPickerSelectionMode.Single -> files.take(1)
        PdfPickerSelectionMode.Multiple -> {
            files
        }
    }
}