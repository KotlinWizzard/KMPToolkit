package com.kmptoolkit.cameraxgallery.gallery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class MediaPickerLauncherState {
    var status by mutableStateOf(MediaPickerLauncherStatus.Idle)
        private set

    fun requestLaunch() {
        status = MediaPickerLauncherStatus.LaunchRequested
    }

    fun launch() {
        status = MediaPickerLauncherStatus.Launched
    }

    fun reset() {
        status = MediaPickerLauncherStatus.Idle
    }
}