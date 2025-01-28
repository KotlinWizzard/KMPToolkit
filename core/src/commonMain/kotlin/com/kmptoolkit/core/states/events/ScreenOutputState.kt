package com.kmptoolkit.core.states.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ScreenOutputState<Output> {
    var status by mutableStateOf<ScreenOutputStatus<Output>>(ScreenOutputStatus.Idle())
        private set

    fun setOutput(data: Output) {
        status = ScreenOutputStatus.SendCallback(data)
    }

    sealed class ScreenOutputStatus<Output> {
        class Idle<Output> : ScreenOutputStatus<Output>()

        data class SendCallback<Output>(
            val data: Output,
        ) : ScreenOutputStatus<Output>()
    }
}