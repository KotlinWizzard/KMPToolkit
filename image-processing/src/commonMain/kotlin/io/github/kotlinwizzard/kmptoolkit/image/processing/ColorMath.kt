package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlin.math.*

internal object ColorMath {
    fun clamp01(x: Float): Float = x.coerceIn(0f, 1f)
    fun clamp255(x: Float): Int = x.roundToInt().coerceIn(0, 255)

    fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val l = (max + min) * 0.5f
        val d = max - min
        if (d == 0f) return floatArrayOf(0f, 0f, l)

        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)

        val h = when (max) {
            r -> ((g - b) / d + (if (g < b) 6f else 0f))
            g -> ((b - r) / d + 2f)
            else -> ((r - g) / d + 4f)
        } / 6f

        return floatArrayOf(h, s, l)
    }

    fun hslToRgb(h: Float, s: Float, l: Float): FloatArray {
        if (s == 0f) return floatArrayOf(l, l, l)

        fun hue2rgb(p: Float, q: Float, tIn: Float): Float {
            var t = tIn
            if (t < 0f) t += 1f
            if (t > 1f) t -= 1f
            if (t < 1f / 6f) return p + (q - p) * 6f * t
            if (t < 1f / 2f) return q
            if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f
            return p
        }

        val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
        val p = 2f * l - q

        val r = hue2rgb(p, q, h + 1f / 3f)
        val g = hue2rgb(p, q, h)
        val b = hue2rgb(p, q, h - 1f / 3f)
        return floatArrayOf(r, g, b)
    }
}