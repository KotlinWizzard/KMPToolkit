package com.kmptoolkit.core.presentation.components.gradient

sealed class GradientBrushType {
    data object Horizontal : GradientBrushType()
    data object Vertical : GradientBrushType()
    data class Degree(val gradientAngle: GradientAngle) : GradientBrushType()
}