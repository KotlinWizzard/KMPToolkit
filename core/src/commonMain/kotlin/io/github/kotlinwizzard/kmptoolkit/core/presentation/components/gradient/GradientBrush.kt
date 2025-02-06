package io.github.kotlinwizzard.kmptoolkit.core.presentation.components.gradient

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

data class GradientBrush(val colors: List<Color>, val type: GradientBrushType) : ShaderBrush() {

    constructor(start: Color, end: Color, type: GradientBrushType) : this(listOf(start, end), type)

    private val brush: LinearGradient = when (type) {
        is GradientBrushType.Degree -> Brush.linearGradientWithRotation(
            colors,
            type.gradientAngle
        ) as LinearGradient

        GradientBrushType.Horizontal -> horizontalGradient(colors) as LinearGradient
        GradientBrushType.Vertical -> verticalGradient(colors) as LinearGradient
    }

    override fun createShader(size: Size): Shader {
        return brush.createShader(size)
    }

    fun reversed() = copy(colors = colors.reversed(), type = type)
}