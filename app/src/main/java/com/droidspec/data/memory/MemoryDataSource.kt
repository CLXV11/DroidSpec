package com.droidspec.data.memory

import android.app.ActivityManager
import android.content.Context
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.model.RamInfo

class MemoryDataSource(context: Context) {
    private val activityManager =
        context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun snapshot(): RamInfo {
        val info = ActivityManager.MemoryInfo()
        return runCatching {
            activityManager.getMemoryInfo(info)
            val used = CalcUtils.usedBytes(info.totalMem, info.availMem)
            RamInfo(
                totalBytes = info.totalMem,
                availableBytes = info.availMem,
                usedBytes = used,
                usagePercent = CalcUtils.percentage(used, info.totalMem) ?: 0,
                thresholdBytes = info.threshold,
                lowMemory = info.lowMemory
            )
        }.getOrElse {
            RamInfo(0L, 0L, 0L, 0, null, null)
        }
    }
}
