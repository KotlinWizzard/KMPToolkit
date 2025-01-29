package com.kmptoolkit.cameraxgallery.gallery

sealed class MediaPickerResult {
    data object Cancelled : MediaPickerResult()
    data class Data(val results: List<MediaPickerResultData>) : MediaPickerResult()
}

data class MediaPickerResultData(val file: String, val mediaType: MediaPickerMediaType)