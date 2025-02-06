package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

sealed class PdfPickerStatus {
    data object Idle : PdfPickerStatus()
    data class LaunchRequested(
        val pdfPickerSelectionMode: PdfPickerSelectionMode = PdfPickerSelectionMode.Single
    ) : PdfPickerStatus()
}