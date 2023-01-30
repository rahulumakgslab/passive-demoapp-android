package com.sonde.base.presentation.utils.util

import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.sonde.base.R
import com.sonde.base.presentation.BaseApplication

fun getContextColor(colorResId: Int): Int {
    return ContextCompat.getColor(BaseApplication.applicationContext(), colorResId)
}
fun getColorByRange(progress: Float, minRange: Float, maxRange: Float): Int {
    val range = (maxRange - minRange) / 9
    if (progress < (minRange + range)) {
        return getContextColor(R.color.track_color1)
    } else if (progress <= (minRange + 2 * range)) {
        val startColor = getContextColor(R.color.track_color1)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.20f)
    } else if (progress <= (minRange + 3 * range)) {
        val startColor = getContextColor(R.color.track_color1)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.50f)
    } else if (progress <= (minRange + 4 * range)) {
        val startColor = getContextColor(R.color.track_color2)
        val endColor = getContextColor(R.color.track_color3)
        return ColorUtils.blendARGB(startColor, endColor, 0.80f)
    } else if (progress <= (minRange + 5 * range)) {
        return getContextColor(R.color.track_color2)
    } else if (progress <= (minRange + 6 * range)) {
        val startColor = getContextColor(R.color.track_color2)
        val endColor = getContextColor(R.color.track_color3)
        return ColorUtils.blendARGB(startColor, endColor, 0.40f)
    } else if (progress <= (minRange + 7 * range)) {
        val startColor = getContextColor(R.color.track_color2)
        val endColor = getContextColor(R.color.track_color3)
        return ColorUtils.blendARGB(startColor, endColor, 0.60f)
    } else if (progress <= (minRange + 8 * range)) {
        val startColor = getContextColor(R.color.track_color2)
        val endColor = getContextColor(R.color.track_color3)
        return ColorUtils.blendARGB(startColor, endColor, 0.70f)
    } else if (progress <= maxRange) {
        val startColor = getContextColor(R.color.track_color2)
        val endColor = getContextColor(R.color.track_color3)
        return ColorUtils.blendARGB(startColor, endColor, 0.80f)
    }
    val startColor = getContextColor(R.color.track_color2)
    val endColor = getContextColor(R.color.track_color3)
    return ColorUtils.blendARGB(startColor, endColor, 0.5f)
}

fun getAggregatedScoreColorByRange(progress: Int): Int {
    // 0 - 64 - 80 - 100
    if (progress <= 30) {
        return getContextColor(R.color.track_color1)
    }else if (progress <= 50) {
        val startColor = getContextColor(R.color.track_color1)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.10f)
    } else if (progress <= 64) {
        val startColor = getContextColor(R.color.track_color1)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.30f)
    } else if (progress <= 79) {
        val startColor = getContextColor(R.color.track_color1)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.95f)
    } else if (progress <= 90) {
        val startColor = getContextColor(R.color.track_color3)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.40f)
    } else if (progress <= 100) {
        val startColor = getContextColor(R.color.track_color3)
        val endColor = getContextColor(R.color.track_color2)
        return ColorUtils.blendARGB(startColor, endColor, 0.10f)
    }

    val startColor = getContextColor(R.color.track_color1)
    val endColor = getContextColor(R.color.track_color2)
    return ColorUtils.blendARGB(startColor, endColor, 0.50f)

}