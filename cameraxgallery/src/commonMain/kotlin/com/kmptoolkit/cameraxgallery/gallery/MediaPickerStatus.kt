package com.kmptoolkit.cameraxgallery.gallery

sealed class MediaPickerStatus {
    data object Idle : MediaPickerStatus()
    data class LaunchRequested(
        val mediaPickerMediaSelectionType: MediaPickerSelectionType = MediaPickerSelectionType.Image,
        val mediaPickerSelectionMode: MediaPickerSelectionMode = MediaPickerSelectionMode.Single
    ) : MediaPickerStatus()
}