package com.droidspec.data.storage

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import android.os.StatFs
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.model.ExternalStorageInfo
import com.droidspec.domain.model.StorageInfo

/**
 * Measures the internal shared-storage partition via StatFs.
 * No file scanning, no permissions required.
 */
class StorageDataSource(context: Context) {
    private val app = context.applicationContext

    fun snapshot(): StorageInfo = runCatching {
        val dataDir = app.filesDir ?: Environment.getDataDirectory()
        val stat = StatFs(dataDir.absolutePath)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val used = CalcUtils.usedBytes(total, free)
        StorageInfo(
            totalBytes = total,
            usedBytes = used,
            freeBytes = free,
            usagePercent = CalcUtils.percentage(used, total) ?: 0
        )
    }.getOrElse {
        StorageInfo(0L, 0L, 0L, 0)
    }

    /** Removable/external storage, when present (SD card or adoptable). */
    fun external(): ExternalStorageInfo? = runCatching {
        val dirs = ContextCompat.getExternalFilesDirs(app, null)
        if (dirs == null || dirs.size < 2) return null
        val ext = dirs[1]
        val stat = StatFs(ext.absolutePath)
        ExternalStorageInfo(
            totalBytes = stat.totalBytes,
            freeBytes = stat.availableBytes,
            removable = Environment.isExternalStorageRemovable(ext)
        )
    }.getOrNull()
}
