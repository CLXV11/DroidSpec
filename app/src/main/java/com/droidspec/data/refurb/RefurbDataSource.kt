package com.droidspec.data.refurb

import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.hardware.camera2.CameraManager
import android.view.InputDevice
import com.droidspec.core.util.CalcUtils
import com.droidspec.data.battery.BatteryDataSource
import com.droidspec.domain.policy.RefurbSignals
import java.io.File

/**
 * Collects refurbishment-check signals. Uses only legitimate sources:
 *  - InputDevice (touch controller identity — changes when the panel is replaced)
 *  - /sys/class/graphics/fb0 panel nodes (panel supplier, when the kernel
 *    exposes it: tianma/boe/csot/...)
 *  - /sys/class/power_supply battery & usb nodes (cycle count, online state)
 *  - Camera2 camera count, display metrics, BatteryManager health/capacity
 * Every read is best-effort; anything not exposed stays null (UNKNOWN),
 * never fabricated.
 */
class RefurbDataSource(
    private val context: Context,
    private val batteryDataSource: BatteryDataSource
) {
    private val app = context.applicationContext

    fun collect(): RefurbSignals {
        val battery = batteryDataSource.snapshot()
        return RefurbSignals(
            touchControllers = touchControllers(),
            panelInfo = panelInfo(),
            batteryCycles = readInt("/sys/class/power_supply/battery/cycle_count")
                ?.takeIf { it in 0..100_000 },
            batteryHealthKey = battery.healthKey,
            capacityPercent = battery.capacityPercent,
            physicalSizeInches = physicalSize(),
            cameraCount = cameraCount(),
            usbState = usbState(),
            fingerprintSupported = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT),
            microphone = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
            speaker = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT),
            vibrator = hasVibrator(),
            chargerType = chargerType(),
            nfcPresent = runCatching {
                (app.getSystemService(Context.NFC_SERVICE) as? NfcManager)?.defaultAdapter != null
            }.getOrNull()
        )
    }

    private val pm: PackageManager get() = app.packageManager

    private fun hasVibrator(): Boolean? = runCatching {
        if (Build.VERSION.SDK_INT >= 31) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator.hasVibrator()
        } else {
            @Suppress("DEPRECATION")
            (app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()
        }
    }.getOrNull()

    private fun chargerType(): String? {
        val candidates = listOf(
            "/sys/class/power_supply/battery/charge_type",
            "/sys/class/power_supply/battery/charger_type",
            "/sys/class/power_supply/usb/type"
        )
        for (path in candidates) {
            val t = readText(path) ?: continue
            val v = t.trim()
            if (v.isNotEmpty() && !v.equals("Unknown", ignoreCase = true)) return v
        }
        return null
    }

    private fun touchControllers(): List<String> = runCatching {
        InputDevice.getDeviceIds().toList().mapNotNull { id ->
            val d = InputDevice.getDevice(id) ?: return@mapNotNull null
            val isTouch = (d.sources and InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN
                || d.name.contains("touch", ignoreCase = true)
            if (!isTouch) return@mapNotNull null
            val vendor = d.vendorId.takeIf { it > 0 }?.let { String.format("%04x", it) }
            val product = d.productId.takeIf { it > 0 }?.let { String.format("%04x", it) }
            buildString {
                append(d.name)
                if (vendor != null && product != null) append(" (vid:$vendor pid:$product)")
            }
        }.distinct()
    }.getOrDefault(emptyList())

    private fun panelInfo(): String? {
        val candidates = listOf(
            "/sys/class/graphics/fb0/msm_fb_panel_info",
            "/sys/class/graphics/fb0/name",
            "/sys/class/graphics/fb0/phy_ctrl"
        )
        for (path in candidates) {
            val text = readText(path) ?: continue
            val useful = text.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .take(4)
            if (useful.isNotEmpty()) return useful.joinToString(" | ")
        }
        return null
    }

    private fun physicalSize(): Double? = runCatching {
        val m = app.resources.displayMetrics
        CalcUtils.screenSizeInches(m.widthPixels, m.heightPixels, m.densityDpi)
    }.getOrNull()

    private fun cameraCount(): Int? = runCatching {
        val cm = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cm.cameraIdList.size
    }.getOrNull()

    private fun usbState(): String? {
        val online = readInt("/sys/class/power_supply/usb/online")
        val present = readInt("/sys/class/power_supply/usb/present")
        return when {
            online != null -> if (online == 1) "USB connected" else "USB idle"
            present != null -> if (present == 1) "USB present" else "USB absent"
            else -> null
        }
    }

    private fun readText(path: String): String? = runCatching {
        val f = File(path)
        if (!f.canRead()) return null
        f.readText().trim().ifBlank { null }
    }.getOrNull()

    private fun readInt(path: String): Int? = runCatching {
        val f = File(path)
        if (!f.canRead()) return null
        f.readText().trim().toIntOrNull()
    }.getOrNull()
}
