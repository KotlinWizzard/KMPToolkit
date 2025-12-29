package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerSelectionType
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerState

@Composable
// Web-only API:
// This composable is fully supported **only on Web targets** (JS & WASM).
// On all other targets (e.g. Android, iOS, Desktop/JVM) this is provided
// as a stub implementation without drag & drop functionality.
expect fun PdfDragAndDropContainer(
    modifier: Modifier = Modifier,
    pdfPickerState: PdfPickerState,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    pickFilesOnClick: Boolean = false
)


@Composable
// Web-only API:
// This composable is fully supported **only on Web targets** (JS & WASM).
// On all other targets (e.g. Android, iOS, Desktop/JVM) this is provided
// as a stub implementation without drag & drop functionality.
fun PdfDragAndDropLayout(
    modifier: Modifier = Modifier,
    pdfPickerState: PdfPickerState,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    pickFilesOnClick: Boolean = false,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        PdfDragAndDropContainer(
            modifier = Modifier.matchParentSize(),
            pdfPickerState = pdfPickerState,
            pdfPickerSelectionMode = pdfPickerSelectionMode,
            pickFilesOnClick = pickFilesOnClick
        )
        content()
    }
}