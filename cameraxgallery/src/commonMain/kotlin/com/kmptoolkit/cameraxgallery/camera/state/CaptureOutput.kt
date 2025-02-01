package com.kmptoolkit.cameraxgallery.camera.state

import com.kmptoolkit.core.service.media.MediaCacheService


sealed class CameraCaptureOutput() {
    data object Error : CameraCaptureOutput()

    data class Success(val mode: CameraCaptureMode, val filePath: String) :
        CameraCaptureOutput() {
        fun readBytes() = MediaCacheService.readCachedFileOrNull(path = filePath)
    }
}