package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

sealed class MediaPickerResult {
    data object Cancelled : io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerResult()
    data class Data(val results: List<io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerResultData>) : io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerResult()
}

data class MediaPickerResultData(val filePath: String, val mediaType: io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerMediaType){
    fun readBytes() = MediaCacheService.readCachedFileOrNull(path = filePath)
}