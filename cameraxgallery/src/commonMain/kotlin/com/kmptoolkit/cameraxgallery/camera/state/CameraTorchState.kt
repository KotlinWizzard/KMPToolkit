package com.kmptoolkit.cameraxgallery.camera.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CameraTorchState {
    var isTorchAvailable: Boolean by mutableStateOf(false)
        private set

    var isTorchEnabled: Boolean by mutableStateOf(false)
        private set

    fun enableTorch() {
        if (isTorchAvailable) {
            isTorchEnabled = true
        }
    }

    fun disableTorch() {
        if (isTorchAvailable) {
            isTorchEnabled = false
        }
    }

    fun setTorchAvailability(isAvailable: Boolean) {
        isTorchAvailable = isAvailable
        if (!isTorchAvailable) {
            isTorchEnabled = false
        }
    }

    fun toggle() {
        if (isTorchAvailable) {
            isTorchEnabled = !isTorchEnabled
        }
    }
}