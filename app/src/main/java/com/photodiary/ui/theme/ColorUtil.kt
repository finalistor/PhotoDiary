package com.photodiary.ui.theme

import androidx.compose.ui.graphics.Color

fun Color.toHsl(): Triple<Float, Float, Float> {
    val r = red
    val g = green
    val b = blue
    val vmax = maxOf(r, g, b)
    val vmin = minOf(r, g, b)
    val delta = vmax - vmin
    val l = (vmax + vmin) / 2f
    if (delta == 0f) return Triple(0f, 0f, l)

    val s = if (l > 0.5f) delta / (2f - vmax - vmin) else delta / (vmax + vmin)

    val h = when (vmax) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    return Triple(if (h < 0) h + 360f else h, s, l)
}
