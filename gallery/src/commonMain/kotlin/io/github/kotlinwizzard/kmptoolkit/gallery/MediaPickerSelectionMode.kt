package io.github.kotlinwizzard.kmptoolkit.gallery

sealed class MediaPickerSelectionMode {
    data object Single : MediaPickerSelectionMode()
    data class Multiple(val maxSelection: Int = INFINITY) : MediaPickerSelectionMode()

    companion object {
        internal const val INFINITY = 0
    }
}