package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.ui.toByteArray
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherState
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherStatus
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService


@Composable
internal actual fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerStatus: PdfPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    when (pdfPickerStatus.pdfPickerSelectionMode) {
        PdfPickerSelectionMode.Single -> LaunchPdfSingle(
            mediaPickerLauncherState = mediaPickerLauncherState,
            onResult = onResult
        )

        PdfPickerSelectionMode.Multiple -> LaunchPdfMultiple(
            mediaPickerLauncherState = mediaPickerLauncherState,
            onResult = onResult
        )
    }
}

@Composable
private fun LaunchPdfSingle(
    mediaPickerLauncherState: MediaPickerLauncherState,
    onResult: (List<PdfPickerResultData>) -> Unit
) {
    val context = LocalContext.current
    val cache = LocalCache.current
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                val result = uri.toPdfPickerResultData(
                    context = context,
                    imageCache = cache.imageCache,
                    pdfCache = cache.pdf
                )
                onResult(listOfNotNull(result))
            },
        )
    LaunchPdfPickerWithLauncher(
        launcher,
        mediaPickerLauncherState
    )
}

@Composable
private fun LaunchPdfMultiple(
    mediaPickerLauncherState: MediaPickerLauncherState,
    onResult: (List<PdfPickerResultData>) -> Unit
) {
    val context = LocalContext.current
    val cache = LocalCache.current
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uris ->
                val result = uris.mapNotNull {
                    it.toPdfPickerResultData(
                        context = context,
                        imageCache = cache.imageCache,
                        pdfCache = cache.pdf
                    )
                }
                onResult(result)
            },
        )
    LaunchPdfPickerWithLauncher(
        launcher,
        mediaPickerLauncherState
    )
}


@Composable
private fun LaunchPdfPickerWithLauncher(
    launcher: ManagedActivityResultLauncher<String, *>,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                launcher.launch(
                    "application/pdf"
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }
}

private fun Uri?.toPdfPickerResultData(
    context: Context,
    imageCache: MediaCacheService.Image,
    pdfCache: MediaCacheService.Pdf
): PdfPickerResultData? {
    if (this == null) return null
    return kotlin.runCatching {
        val contentResolver = context.contentResolver
        val parcelFileDescriptor = contentResolver.openFileDescriptor(this, "r") ?: return null
        val bytes = readBytes(contentResolver) ?: return null
        val filename =
            filename(contentResolver) ?: pdfCache.generateFilename()
        val pdfFilePath = pdfCache.cacheFileTemporary(
            bytes,
            filename
        )
        val renderer = PdfRenderer(parcelFileDescriptor)
        val totalPages = renderer.pageCount
        val imagePreviewPath = imageCache.cacheFileTemporary(
            renderer.getPageAsBitmap(0).toByteArray(),
            filename = imageCache.generateFilename(".png")
        )
        renderer.close()
        PdfPickerResultData(
            filePath = pdfFilePath,
            pages = totalPages,
            previewImageFilePath = imagePreviewPath, filename = filename
        )
    }.getOrNull()
}

private fun Uri.filename(contentResolver: ContentResolver): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    var fileName: String? = null
    val cursor: Cursor =
        contentResolver.query(this, projection, null, null, null) ?: return null
    cursor.use { metaCursor ->
        if (metaCursor.moveToFirst()) {
            fileName = metaCursor.getString(0)
        }
    }
    return fileName
}

private fun Uri?.readBytes(contentResolver: ContentResolver): ByteArray? {
    val uri = this ?: return null
    return when (uri.scheme) {
        "file" -> {
            uri.toFile().readBytes()
        }

        "content" -> {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        }

        else -> {
            null
        }
    }
}

private fun PdfRenderer.getPageAsBitmap(pageIndex: Int = 0): Bitmap {
    val page = openPage(pageIndex)
    val mBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mBitmap)
    canvas.drawColor(Color.WHITE)
    page.render(mBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    page.close()
    return mBitmap
}
