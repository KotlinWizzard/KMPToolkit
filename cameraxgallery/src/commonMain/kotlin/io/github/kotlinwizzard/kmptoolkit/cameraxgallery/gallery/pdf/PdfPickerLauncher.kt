package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import androidx.compose.runtime.Composable
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherState

@Composable
expect internal fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerStatus: PdfPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
)