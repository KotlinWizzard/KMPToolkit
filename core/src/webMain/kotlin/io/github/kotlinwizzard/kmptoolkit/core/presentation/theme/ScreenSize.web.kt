package io.github.kotlinwizzard.kmptoolkit.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize

@Composable
internal actual fun getScreenSize(): IntSize {
    return LocalWindowInfo.current.containerSize
}