package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import androidx.compose.runtime.Composable
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherState

@Composable
internal actual fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerStatus: PdfPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
}