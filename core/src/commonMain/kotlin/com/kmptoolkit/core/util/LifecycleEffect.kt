package com.kmptoolkit.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun LifecycleEffect(
    onStarted: () -> Unit = {},
    onStopped: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
) {
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(Unit, effect = {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> Unit
                    Lifecycle.Event.ON_START -> onStarted()
                    Lifecycle.Event.ON_RESUME -> onResume()
                    Lifecycle.Event.ON_PAUSE -> onPause()
                    Lifecycle.Event.ON_STOP -> onStopped()
                    Lifecycle.Event.ON_DESTROY -> Unit
                    Lifecycle.Event.ON_ANY -> Unit
                }
            }
        lifecycle.lifecycle.addObserver(observer)
        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
        }
    })
}