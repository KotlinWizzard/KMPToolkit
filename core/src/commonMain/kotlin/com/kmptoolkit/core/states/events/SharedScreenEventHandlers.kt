package com.kmptoolkit.core.states.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

@Composable
inline fun <reified T> LaunchListenScreenOutput(
    receiverKey: String, crossinline onReceive: (T) -> Unit
) {
    val scope = rememberCoroutineScope()
    val screenEventManger = LocalSharedScreenEventManager.current
    screenEventManger.SharedScreenEffect<ScreenOutputEvent<T>>(key = receiverKey, scope = scope) {
        it.receiverData?.let(onReceive)
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun <T> LaunchListenScreenOutputWithCastedType(
    receiverKey: String, onReceive: (T) -> Unit
) {
    val scope = rememberCoroutineScope()
    val screenEventManger = LocalSharedScreenEventManager.current
    screenEventManger.SharedScreenEffect<ScreenOutputEvent<*>>(key = receiverKey, scope = scope) {
        (it.receiverData as? T)?.let { data ->
            onReceive.invoke(data)
        }
    }
}



@Composable
inline fun <reified T> LaunchSetScreenOutput(
    receiverKey: String,
    callbackState: ScreenOutputState<T>,
    crossinline onFinish: () -> Unit = {},
) {
    var eventSent by remember(receiverKey) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (eventSent) return

    val sharedEvents = LocalSharedScreenEventManager.current

    val status = callbackState.status
    LaunchedEffect(status, scope) {
        if (status is ScreenOutputState.ScreenOutputStatus.SendCallback<T>) {
            sharedEvents.emitEventSuspended(
                ScreenOutputEvent(
                    receiverKey = receiverKey,
                    receiverData = status.data,
                ),
            )
            onFinish.invoke()
            eventSent = true
        }
    }
}


inline fun <reified T> setScreenOutputImmediately(
    receiverKey: String,
    data: T,
    sharedEvents: com.kmptoolkit.core.states.events.SharedScreenEventManager,
) {
    sharedEvents.emitEvent(
        ScreenOutputEvent(
            receiverKey = receiverKey,
            receiverData = data,
        ),
    )
}