package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery

sealed class MediaPickerStatus {
    data object Idle : io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerStatus()
    data class LaunchRequested(
        val mediaPickerMediaSelectionType: io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionType = io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionType.Image,
        val mediaPickerSelectionMode: io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionMode = io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerSelectionMode.Single
    ) : io.github.kotlinwizzard.kmptoolkit.cameraxgallery.gallery.MediaPickerStatus()
}