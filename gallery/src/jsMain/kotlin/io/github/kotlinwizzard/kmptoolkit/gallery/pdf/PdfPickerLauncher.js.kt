package io.github.kotlinwizzard.kmptoolkit.gallery.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerLauncherState
import io.github.kotlinwizzard.kmptoolkit.gallery.MediaPickerLauncherStatus
import io.github.kotlinwizzard.kmptoolkit.gallery.chooseFile
import io.github.kotlinwizzard.kmptoolkit.gallery.readFileAsByteArray
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    val status = mediaPickerLauncherState.status
    val document = document
    val coroutineScope = rememberCoroutineScope()
    val localCache = LocalCache.current
    initPdfJs()
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                coroutineScope.launch(Dispatchers.IO) {
                    chooseFile(
                        document,
                        onResult = {
                            onResult(it.mapFiles(imageCache = localCache.imageCache, pdfCacheService = localCache.pdf))
                        },
                        multiple = pdfPickerSelectionMode == PdfPickerSelectionMode.Multiple,
                        extensions = listOf("pdf")
                    )
                }
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}

private fun initPdfJs() {
    GlobalWorkerOptions.workerSrc = pdfWorkerSrc
}

private suspend fun List<File>.mapFiles(imageCache: MediaCacheService.Image, pdfCacheService: MediaCacheService.Pdf): List<PdfPickerResultData> {
    return mapNotNull { file ->
        runCatching {
            // nur PDFs zulassen
            val mime = file.type.lowercase()
            val nameLower = file.name.lowercase()
            if (!(mime == "application/pdf" || nameLower.endsWith(".pdf"))) {
                return@runCatching null
            }

            // 1) Datei als ArrayBuffer lesen
            val arrayBuffer = file.readAsArrayBuffer()
            if (arrayBuffer.byteLength == 0) return@runCatching null

            // 2) ByteArray für pdfCache erzeugen
            val pdfBytes = arrayBuffer.toByteArray()

            // 3) PDF im Cache speichern
            val pdfPath = pdfCacheService.cacheFileTemporary(
                content = pdfBytes,
                filename = file.name
            )

            // 4) pdf.js Document laden
            val loadingTask =getDocument(js("{ data: arrayBuffer }"))
            val pdfDoc = loadingTask.promise.await()

            // Seitenanzahl
            val totalPages = pdfDoc.numPages

            // 5) erste Seite rendern
            val page = pdfDoc.getPage(1).await()
            val viewport = page.getViewport(js("{ scale: 1.0 }"))

            val canvas = (document.createElement("canvas") as HTMLCanvasElement).apply {
                width = viewport.width.toInt()
                height = viewport.height.toInt()
            }

            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D?
                ?: return@runCatching null

            val renderTask = page.render(
                js(
                    """({
                        canvasContext: ctx,
                        viewport: viewport
                    })"""
                )
            )
            renderTask.promise.await()

            // 6) Canvas -> PNG-Bytes
            val dataUrl = canvas.toDataURL("image/png")
            val previewBytes = dataUrlToByteArray(dataUrl)

            // 7) Preview im Image-Cache speichern
            val previewPath = imageCache.cacheFileTemporary(
                content = previewBytes,
                filename = imageCache.generateFilename(".png")
            )

            // 8) Ergebnisobjekt
            PdfPickerResultData(
                filePath = pdfPath,
                filename = file.name,
                previewImageFilePath = previewPath,
                pages = totalPages
            )
        }.apply {
            exceptionOrNull()?.let {
                throw it
            }
        }.getOrNull()
    }
}

private suspend fun File.readAsArrayBuffer(): ArrayBuffer =
    suspendCoroutine { cont ->
        val reader = FileReader()
        reader.onload = {
            val result = reader.result
            if (result is ArrayBuffer) {
                cont.resume(result)
            } else {
                cont.resume(ArrayBuffer(0))
            }
        }
        reader.onerror = {
            cont.resume(ArrayBuffer(0))
        }
        reader.readAsArrayBuffer(this)
    }

// ArrayBuffer → ByteArray (für deinen Cache)
private fun ArrayBuffer.toByteArray(): ByteArray {
    val int8 = Int8Array(this)
    return ByteArray(int8.length) { index -> int8[index] }
}

external fun atob(data: String): String

private fun dataUrlToByteArray(dataUrl: String): ByteArray {
    val base64Part = dataUrl.substringAfter("base64,", "")
    if (base64Part.isEmpty()) return ByteArray(0)

    val binaryString = atob(base64Part)
    val length = binaryString.length
    val bytes = ByteArray(length)
    for (i in 0 until length) {
        bytes[i] = binaryString[i].code.toByte()
    }
    return bytes
}