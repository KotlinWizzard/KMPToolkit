package io.github.kotlinwizzard.kmptoolkit.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerState
import io.github.kotlinwizzard.kmptoolkit.gallery.pdf.PdfPickerState

@Composable
actual fun MediaImageOrPdfDragAndDropContainer(
    modifier: Modifier,
    mediaPickerState: MediaPickerState,
    pdfPickerState: PdfPickerState,
    maxImages: Int,
    pickFilesOnClick: Boolean
) {
}