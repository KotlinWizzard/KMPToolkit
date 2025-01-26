package states.events

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSharedScreenEventManager = staticCompositionLocalOf { SharedScreenEventManager.INSTANCE }

