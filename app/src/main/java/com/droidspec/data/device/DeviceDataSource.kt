package com.droidspec.data.device

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.policy.DeviceReleaseEstimator
import com.droidspec.domain.model.DeviceInfo
import com.droidspec.domain.model.RootHeuristic
import com.droidspec.domain.model.RootIndicator
import java.io.File

/** Reads Build fields and a few well-known, non-invasive root heuristics. */
class DeviceDataSource(private val context: Context) {
    private val powerManager =
        context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager


    fun snapshot(): DeviceInfo {
        val abi = Build.SUPPORTED_ABIS.firstOrNull()
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.ifBlank { null },
            brand = Build.BRAND.ifBlank { null },
            model = Build.MODEL.ifBlank { null },
            device = Build.DEVICE.ifBlank { null },
            product = Build.PRODUCT.ifBlank { null },
            board = Build.BOARD.ifBlank { null },
            hardware = Build.HARDWARE.ifBlank { null },
            bootloader = Build.BOOTLOADER.ifBlank { null },
            baseband = runCatching { Build.getRadioVersion() }.getOrNull()?.ifBlank { null },
            androidVersion = Build.VERSION.RELEASE.ifBlank { null },
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= 23) {
                Build.VERSION.SECURITY_PATCH.ifBlank { null }
            } else null,
            buildId = Build.ID.ifBlank { null },
            buildFingerprint = Build.FINGERPRINT.ifBlank { null },
            kernelVersion = System.getProperty("os.version")?.ifBlank { null },
            architecture = abi ?: System.getProperty("os.arch"),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            rootIndicator = RootIndicator(detectRootHeuristic()),
            uptimeMs = runCatching { SystemClock.elapsedRealtime() }.getOrNull(),
            buildDateMs = Build.TIME,
            releaseYear = DeviceReleaseEstimator.estimate(Build.DEVICE),
            deepSleepPercent = runCatching {
                CalcUtils.deepSleepPercent(
                    SystemClock.elapsedRealtime(),
                    SystemClock.uptimeMillis()
                )
            }.getOrNull(),
            selinuxStatus = readSelinuxStatus(),
            bootCount = runCatching {
                Settings.Global.getInt(context.contentResolver, "boot_count")
            }.getOrNull(),
            interactive = runCatching { powerManager.isInteractive }.getOrNull(),
            idleMode = if (Build.VERSION.SDK_INT >= 23) {
                runCatching { powerManager.isDeviceIdleMode }.getOrNull()
            } else null
        )
    }

    private fun readSelinuxStatus(): String? = runCatching {
        val f = File("/sys/fs/selinux/enforce")
        if (!f.canRead()) return null
        when (f.readText().trim()) {
            "1" -> "Enforcing"
            "0" -> "Permissive"
            else -> null
        }
    }.getOrNull()

    /**
     * Non-authoritative indicator only. A device may be rooted without any of
     * these artifacts, and an emulator may trip them without being "rooted".
     */
    private fun detectRootHeuristic(): RootHeuristic = runCatching {
        if (Build.TAGS?.contains("test-keys") == true) return RootHeuristic.LIKELY_PRESENT
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        if (paths.any { File(it).exists() }) RootHeuristic.LIKELY_PRESENT
        else RootHeuristic.NOT_DETECTED
    }.getOrDefault(RootHeuristic.UNKNOWN)
}
