package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherState
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerLauncherStatus
import io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.toByteArray
import io.github.kotlinwizzard.kmptoolkit.core.service.media.LocalCache
import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGPDFBox
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PDFKit.PDFDisplayBox
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFPage
import platform.PDFKit.kPDFDisplayBoxMediaBox
import platform.UIKit.UIImage
import platform.UIKit.UIScreen

@Composable
internal actual fun LaunchPdfPicker(
    onResult: (List<PdfPickerResultData>) -> Unit,
    pdfPickerStatus: PdfPickerStatus.LaunchRequested,
    mediaPickerLauncherState: MediaPickerLauncherState
) {
   LaunchPicker(
       pdfPickerSelectionMode = pdfPickerStatus.pdfPickerSelectionMode,
       mediaPickerLauncherState = mediaPickerLauncherState,
       onResult
   )
}

@Composable
private  fun LaunchPicker(pdfPickerSelectionMode: PdfPickerSelectionMode,
                          mediaPickerLauncherState: MediaPickerLauncherState, onResult: (List<PdfPickerResultData>) -> Unit,){
    val scope = rememberCoroutineScope()
    val cache = LocalCache.current
    val delegate =  DocumentPickerDelegate(
        onFilesPicked = { urls->
            val dispatchGroup = dispatch_group_create()
            val result = mutableListOf<PdfPickerResultData>()
            urls.forEach {
                dispatch_group_enter(dispatchGroup)
                val pdfResult = it.getResult(
                    cache.imageCache,
                    cache.pdf
                )
                if(pdfResult!=null) {
                    result.add(pdfResult)
                }
                dispatch_group_leave(dispatchGroup)

            }
            dispatch_group_notify(dispatchGroup, platform.darwin.dispatch_get_main_queue()) {
                scope.launch(Dispatchers.Main) {
                    onResult(result)
                }
            }
        },
        onPickerCancelled = {
            onResult.invoke(emptyList())
        }
    )
    val status = mediaPickerLauncherState.status
    LaunchedEffect(status) {
        when (status) {
            MediaPickerLauncherStatus.LaunchRequested -> {
                val pickerController  = UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypePDF),
                    asCopy = true
                )
                pickerController.allowsMultipleSelection = pdfPickerSelectionMode== PdfPickerSelectionMode.Multiple
                pickerController.delegate = delegate
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerController,
                    true,
                    null,
                )
                mediaPickerLauncherState.launch()
            }

            else -> Unit
        }
    }

}

private fun NSURL.getResult(
    imageCacheService: MediaCacheService.Image,
    pdfCacheService: MediaCacheService.Pdf
):PdfPickerResultData?{
   return kotlin.runCatching {
        val data = NSData.dataWithContentsOfURL(this) ?: return null
       val fileName = this.lastPathComponent ?: pdfCacheService.generateFilename()
       val bytes = data.toByteArray() ?: return null
       val pdfFilePath = pdfCacheService.cacheFileTemporary(bytes, fileName)
       val pdfDocument = PDFDocument(this)
       val pageCount = pdfDocument.pageCount
       val image = pdfDocument.getPdfPreview()
       val imageBytes = image?.toByteArray() ?: return null
       val imageUrl = imageCacheService.cacheFileTemporary(imageBytes)
       return PdfPickerResultData(
           filePath = pdfFilePath,
           fileName,
           pages = pageCount.toInt(),
           previewImageFilePath = imageUrl
       )
    }.getOrNull()
}

@OptIn(ExperimentalForeignApi::class)
fun PDFDocument.getPdfPreview(): UIImage? {
    val firstPage = pageAtIndex(0u) ?: return null
    val size = CGSizeMake(800.0, 800.0)
    val image = firstPage.thumbnailOfSize(size,kPDFDisplayBoxMediaBox)
    return image
}
