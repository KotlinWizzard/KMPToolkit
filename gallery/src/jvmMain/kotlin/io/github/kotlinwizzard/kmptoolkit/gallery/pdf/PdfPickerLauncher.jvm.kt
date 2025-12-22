package io.github.kotlinwizzard.kmptoolkit.gallery.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerLauncherState
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerLauncherStatus
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerMediaType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal actual fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerStatus: PdfPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    LaunchPdfPicker(
        onResult = onResult,
        pdfPickerSelectionMode = pdfPickerStatus.pdfPickerSelectionMode,
        mediaPickerLauncherState
    )
}


@Composable
internal fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val localCache = LocalCache.current

    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                chooseFile(
                    onResult = onResult,
                   pdfPickerSelectionMode = pdfPickerSelectionMode,
                    imageCache = localCache.imageCache,
                    pdfCache = localCache.pdf
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}

internal fun chooseFile(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerSelectionMode: PdfPickerSelectionMode,
    imageCache: MediaCacheService.Image,
    pdfCache: MediaCacheService.Pdf
) {
    val chooser = JFileChooser().apply {
        isMultiSelectionEnabled = pdfPickerSelectionMode == PdfPickerSelectionMode.Multiple
        fileFilter = FileNameExtensionFilter(
            "Documents",
            "pdf",
        )

    }

    val result = chooser.showOpenDialog(null)

    if (result == JFileChooser.APPROVE_OPTION) {
        val files: List<File> =
            if (chooser.isMultiSelectionEnabled) {
                chooser.selectedFiles.toList()
            } else {
                listOfNotNull(chooser.selectedFile)
            }

        val picked: List<PdfPickerResultData> =
            files.mapNotNull { file ->
                file.toPdfPickerResultData(
                    imageCache = imageCache,
                    pdfCache = pdfCache
                )
            }

        // 2) Ergebnis nach außen geben
        onResult(picked)
    } else {
        // ggf. leere Liste oder gar nichts tun, je nach deinem State-Handling
        onResult(emptyList())
    }
}

internal fun File.toPdfPickerResultData(
    imageCache: MediaCacheService.Image,
    pdfCache: MediaCacheService.Pdf
): PdfPickerResultData? {
    return runCatching {
        // PDF Bytes
        val bytes = readBytes()

        // Dateiname wie auf Android
        val filename = name.ifBlank { pdfCache.generateFilename() }

        // PDF im Cache ablegen
        val pdfFilePath = pdfCache.cacheFileTemporary(
            bytes,
            filename
        )

        // PDF mit PDFBox öffnen

        val document = PDDocument.load(this)
        val renderer = PDFRenderer(document)

        // Seitenanzahl
        val totalPages = document.numberOfPages

        // Erste Seite rendern
        val bufferedImage: BufferedImage = renderer.renderImageWithDPI(
            0,
            150f // [Inference] DPI nach Bedarf anpassen
        )

        // Bitmap als PNG in ByteArray konvertieren
        val previewBytes = ByteArrayOutputStream().use { out ->
            ImageIO.write(bufferedImage, "png", out)
            out.toByteArray()
        }

        document.close()

        // Preview-Bild über deinen Image Cache speichern
        val imagePreviewPath = imageCache.cacheFileTemporary(
            previewBytes,
            filename = imageCache.generateFilename(".png")
        )

        PdfPickerResultData(
            filePath = pdfFilePath,
            pages = totalPages,
            previewImageFilePath = imagePreviewPath,
            filename = filename
        )
    }.getOrNull()
}