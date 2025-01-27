package presentation.components.gradient

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Brush.Companion.linearGradientWithRotation(colors: List<Color>, angle: GradientAngle): Brush {
    val gradientOffset = GradientOffset.getByAngle(angle)
    return linearGradient(
        colors = colors,
        start = gradientOffset.start,
        end = gradientOffset.end
    )
}