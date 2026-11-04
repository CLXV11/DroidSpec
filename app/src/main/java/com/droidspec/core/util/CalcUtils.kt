package com.droidspec.core.util

import kotlin.math.roundToInt

/** Pure calculation helpers. Unit-testable, no Android dependencies. */
object CalcUtils {

    /** Percentage (0..100) of [part] of [total], rounded. Null when total <= 0. */
    fun percentage(part: Long, total: Long): Int? {
        if (total <= 0L) return null
        return ((part * 100.0) / total).roundToInt().coerceIn(0, 100)
    }

    /** Used bytes = total - free, never negative. */
    fun usedBytes(total: Long, free: Long): Long = (total - free).coerceAtLeast(0L)

    /** Convert Hz (e.g. from cpufreq sysfs) to MHz, or null when invalid. */
    fun kHzToMhz(kHz: Long?): Double? =
        kHz?.takeIf { it > 0 }?.let { it / 1000.0 }

    /** Megapixels from pixel array size. */
    fun megapixels(width: Int, height: Int): Double =
        width.toDouble() * height.toDouble() / 1_000_000.0

    /** Deep-sleep percentage since boot: (elapsed - uptime) / elapsed. Null if invalid. */
    fun deepSleepPercent(elapsedMs: Long, uptimeMs: Long): Int? {
        if (elapsedMs <= 0) return null
        return (((elapsedMs - uptimeMs).coerceAtLeast(0)) * 100 / elapsedMs)
            .toInt().coerceIn(0, 100)
    }

    /** Qualitative density bucket name from dpi, e.g. "xxhdpi". Null when dpi <= 0. */
    fun densityBucket(dpi: Int): String? = when {
        dpi <= 0 -> null
        dpi < 140 -> "ldpi"
        dpi < 200 -> "mdpi"
        dpi < 280 -> "hdpi"
        dpi < 400 -> "xhdpi"
        dpi < 560 -> "xxhdpi"
        else -> "xxxhdpi"
    }

    /** Splits a duration (ms) into days/hours/minutes for display. */
    fun splitDuration(ms: Long): Triple<Int, Int, Int> {
        val totalMin = (ms / 60_000).coerceAtLeast(0)
        val days = (totalMin / 1440).toInt()
        val hours = ((totalMin % 1440) / 60).toInt()
        val min = (totalMin % 60).toInt()
        return Triple(days, hours, min)
    }

    /** Physical screen size in inches from pixels and dpi. Null when dpi <= 0. */
    fun screenSizeInches(widthPx: Int, heightPx: Int, densityDpi: Int): Double? {
        if (densityDpi <= 0) return null
        val w = widthPx.toDouble() / densityDpi
        val h = heightPx.toDouble() / densityDpi
        return kotlin.math.sqrt(w * w + h * h)
    }
}
