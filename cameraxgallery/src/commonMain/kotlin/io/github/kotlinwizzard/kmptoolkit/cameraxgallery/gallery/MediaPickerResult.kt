package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

sealed class MediaPickerResult {
    data object Cancelled : MediaPickerResult()
    data class Data(val results: List<MediaPickerResultData>) : MediaPickerResult()
}

data class MediaPickerResultData(val filePath: String, val mediaType: MediaPickerMediaType){
    fun readBytes() = MediaCacheService.readCachedFileOrNull(path = filePath)
}