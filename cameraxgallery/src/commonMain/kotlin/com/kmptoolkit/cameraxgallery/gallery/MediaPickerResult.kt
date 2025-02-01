package com.kmptoolkit.cameraxgallery.gallery

import com.kmptoolkit.core.service.media.MediaCacheService

sealed class MediaPickerResult {
    data object Cancelled : MediaPickerResult()
    data class Data(val results: List<MediaPickerResultData>) : MediaPickerResult()
}

data class MediaPickerResultData(val filePath: String, val mediaType: MediaPickerMediaType){
    fun readBytes() = MediaCacheService.readCachedFileOrNull(path = filePath)
}