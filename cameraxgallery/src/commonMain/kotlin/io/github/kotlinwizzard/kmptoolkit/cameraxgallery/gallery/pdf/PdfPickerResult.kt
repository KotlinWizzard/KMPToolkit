package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.pdf

import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

sealed class PdfPickerResult {
    data object Cancelled : PdfPickerResult()
    data class Data(val results: List<PdfPickerResultData>) : PdfPickerResult()
}

data class PdfPickerResultData(
    val filePath: String,
    val previewImageFilePath: String,
    val pages: Int
) {
    fun readBytes() = MediaCacheService.readCachedFileOrNull(path = filePath)
    fun readPreviewImageBytes() = MediaCacheService.readCachedFileOrNull(path = previewImageFilePath)
}