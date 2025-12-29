package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerSelectionMode
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerState

@Composable
actual fun PdfDragAndDropContainer(
    modifier: Modifier,
    pdfPickerState: PdfPickerState,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    pickFilesOnClick: Boolean
) {
}