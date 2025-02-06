package io.github.kotlinwizzard.kmptoolkit.core.states.events

data class ScreenOutputEvent<Output>(
    val receiverKey: String,
    val receiverData: Output,
) : SharedScreenEvent()