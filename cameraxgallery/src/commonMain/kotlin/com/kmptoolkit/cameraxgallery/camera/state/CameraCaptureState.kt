package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class CameraCaptureState(protected open val onCapture: (String?) -> Unit) {
    var isCapturing: Boolean by mutableStateOf(false)
        protected set


    internal fun onCapture(filePath: String?) {
        onCapture.invoke(filePath)
        cleanup()
    }

    internal var onFrame: ((frame: ByteArray) -> Unit)? = null

    fun capture() {
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
            stopCapturing()
        }
    }

    data class Image(override val onCapture: (String?) -> Unit) : CameraCaptureState(onCapture) {

        internal var triggerCaptureAnchor: (() -> Unit)? = null

        internal fun onCapture(image: ByteArray?) {

        }
    }

    data class Video(override val onCapture: (String?) -> Unit) : CameraCaptureState(onCapture) {
        var recordedDurationNanos by mutableStateOf(0L)
            internal set

        override fun cleanup() {
            recordedDurationNanos = 0
        }
    }
}