package presentation.components.focus

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalParentFocusState: ProvidableCompositionLocal<FocusLayoutState?> =
    compositionLocalOf { null }