package io.github.kotlinwizzard.kmptoolkit.core.states.events

import kotlinx.serialization.Serializable

@Serializable
abstract class SharedScreenEvent {
    var collected: Boolean = false
}