package com.droidspec.data.display

import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.model.DisplayInfo

class DisplayDataSource(private val context: Context) {
    private val app = context.applicationContext

    @Suppress("DEPRECATION")
    fun snapshot(): DisplayInfo {
        val metrics = app.resources.displayMetrics
        // API 30+: window metrics are exact even in split-screen/multi-window
        val bounds = if (Build.VERSION.SDK_INT >= 30) {
            runCatching {
                val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.currentWindowMetrics.bounds
            }.getOrNull()
        } else null
        val widthPx = bounds?.width() ?: metrics.widthPixels
        val heightPx = bounds?.height() ?: metrics.heightPixels
        val display: Display? = if (Build.VERSION.SDK_INT >= 30) {
            val dm = app.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            dm.getDisplay(Display.DEFAULT_DISPLAY)
        } else {
            val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay
        }

        val modes = runCatching {
            display?.supportedModes?.map { mode ->
                "${mode.physicalWidth}x${mode.physicalHeight} @ ${mode.refreshRate.toInt()} Hz"
            }
        }.getOrNull() ?: emptyList()

        val hdr = runCatching {
            display?.hdrCapabilities?.supportedHdrTypes?.map { type ->
                when (type) {
                    Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
                    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
                    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
                    Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
                    else -> "Type $type"
                }
            }
        }.getOrNull() ?: emptyList()

        val physical = CalcUtils.screenSizeInches(widthPx, heightPx, metrics.densityDpi)

        val refresh = runCatching {
            if (Build.VERSION.SDK_INT >= 30) display?.refreshRate
            else display?.refreshRate
        }.getOrNull()

        return DisplayInfo(
            widthPx = widthPx.takeIf { it > 0 },
            heightPx = heightPx.takeIf { it > 0 },
            density = metrics.density.takeIf { it > 0f },
            densityDpi = metrics.densityDpi.takeIf { it > 0 },
            refreshRateHz = refresh,
            modes = modes,
            hdrTypes = hdr,
            physicalSizeInches = physical,
            densityBucket = CalcUtils.densityBucket(metrics.densityDpi),
            smallestWidthDp = runCatching {
                (minOf(widthPx, heightPx) * 160) / metrics.densityDpi
            }.getOrNull(),
            orientationKey = when (app.resources.configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> "portrait"
                Configuration.ORIENTATION_LANDSCAPE -> "landscape"
                Configuration.ORIENTATION_SQUARE -> "square"
                else -> "undefined"
            }
        )
    }
}
