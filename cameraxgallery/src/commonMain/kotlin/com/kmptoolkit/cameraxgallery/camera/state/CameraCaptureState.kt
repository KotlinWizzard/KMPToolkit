package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kmptoolkit.core.extensions.formattedMinuteSecondsString
import com.kmptoolkit.core.service.image.CacheService
import kotlin.time.Duration.Companion.nanoseconds

sealed class CameraCaptureState(protected open val onCapture: (String?) -> Unit) {
    var isCapturing: Boolean by mutableStateOf(false)
        protected set


    internal fun onCapture(filePath: String?) {
        onCapture.invoke(filePath)
        cleanup()
    }

    internal var onFrame: ((frame: ByteArray) -> Unit)? = null

    open fun capture() {
        isCapturing = true
    }

    fun stopCapturing() {
        isCapturing = false
    }

    protected open fun cleanup() = Unit


    fun toggleCapture() {
        if (isCapturing) {
            stopCapturing()
        } else {
            capture()
        }
    }

    data class Image(
        override val onCapture: (String?) -> Unit,
        internal var imageCompressionMode:ImageCompressionMode = ImageCompressionMode.None
    ) : CameraCaptureState(onCapture) {

        override fun capture() {
            super.capture()
            triggerCaptureAnchor?.invoke()
        }

        internal var triggerCaptureAnchor: (() -> Unit)? = null

        internal fun onCapture(image: ByteArray?, imageCacheService: CacheService.Image) {
            if (image != null) {
                val compressedBytes = imageCompressionMode.compress(image)
                onCapture(filePath = imageCacheService.cacheFileTemporary(content = compressedBytes))
            } else {
                onCapture(filePath = null)
            }
        }
    }

    data class Video(override val onCapture: (String?) -> Unit) : CameraCaptureState(onCapture) {
        var recordedDurationNanos by mutableStateOf(0L)
            internal set

        val minuteSecondsText:String? by derivedStateOf {
            println("=== minutes: $recordedDurationNanos, isCapturing:$isCapturing")
            if(!isCapturing) return@derivedStateOf null
            recordedDurationNanos.nanoseconds.formattedMinuteSecondsString()
        }

        override fun cleanup() {
            recordedDurationNanos = 0
        }
    }
}