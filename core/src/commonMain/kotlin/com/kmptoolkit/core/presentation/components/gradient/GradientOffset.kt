package com.kmptoolkit.core.presentation.components.gradient

import androidx.compose.ui.geometry.Offset

internal data class GradientOffset(val start: Offset, val end: Offset) {
    companion object {
        fun getByAngle(angle: GradientAngle = GradientAngle.Degree0): GradientOffset {
            return when (angle) {
                GradientAngle.Degree45 -> GradientOffset(
                    start = Offset.Zero,
                    end = Offset.Infinite
                )

                GradientAngle.Degree90 -> GradientOffset(
                    start = Offset.Zero,
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )

                GradientAngle.Degree135 -> GradientOffset(
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )

                GradientAngle.Degree180 -> GradientOffset(
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end = Offset.Zero
                )

                GradientAngle.Degree225 -> GradientOffset(
                    start = Offset.Infinite,
                    end = Offset.Zero
                )

                GradientAngle.Degree270 -> GradientOffset(
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset.Zero
                )

                GradientAngle.Degree315 -> GradientOffset(
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )

                else -> GradientOffset(
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            }
        }
    }
}