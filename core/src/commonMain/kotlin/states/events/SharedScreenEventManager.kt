package states.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import util.io

@Serializable
class SharedScreenEventManager(private val bufferSize: Int = Int.MAX_VALUE) {
    private val _screenEvents: MutableSharedFlow<SharedScreenEvent> =
        MutableSharedFlow(replay = bufferSize)
    val screenEvents: SharedFlow<SharedScreenEvent> = _screenEvents

    fun emitEvent(
        sharedScreenEvent: SharedScreenEvent,
        scope: CoroutineScope = CoroutineScope(Dispatchers.io),
    ) {
        scope.launch {
            emitEventSuspended(sharedScreenEvent)
        }
    }

    suspend fun emitEventSuspended(sharedScreenEvent: SharedScreenEvent) {
        _screenEvents.emit(sharedScreenEvent)
    }

    @Composable
    inline fun <reified T : SharedScreenEvent> SharedScreenEffect(
        key: String? = null,
        scope: CoroutineScope = rememberCoroutineScope(),
        crossinline shouldCollect: (event: T) -> Boolean = { event ->
            when (event) {
                is ScreenOutputEvent<*> -> {
                    event.receiverKey == key
                }

                else -> true
            }
        },
        crossinline onEvent: (T) -> Unit,
    ) {
        val events = screenEvents
        DisposableEffect(key1 = key, key2 = scope) {
            val job =
                scope.launch {
                    events.collectLatest { event ->
                        if (!event.collected && event is T) {
                            if (shouldCollect.invoke(event)) {
                                event.collected = true
                                onEvent(event)
                            }
                        }
                    }
                }
            onDispose { job.cancel() }
        }
    }
}