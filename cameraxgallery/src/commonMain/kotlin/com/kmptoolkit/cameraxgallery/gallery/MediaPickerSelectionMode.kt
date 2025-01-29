package com.kmptoolkit.cameraxgallery.gallery

sealed class MediaPickerSelectionMode {
    data object Single : MediaPickerSelectionMode()
    data class Multiple(val maxSelection: Int = INFINITY) : MediaPickerSelectionMode()

    companion object {
        internal const val INFINITY = 0
    }
}