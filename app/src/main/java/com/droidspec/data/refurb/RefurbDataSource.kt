package com.droidspec.data.refurb

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.nfc.NfcManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.view.InputDevice
import com.droidspec.core.util.CalcUtils
import com.droidspec.data.battery.BatteryDataSource
import com.droidspec.domain.policy.HardwareIdParsers
import com.droidspec.domain.policy.RefurbSignals
import java.io.File

/**
 * Deep hardware signal collection. Combines public APIs with best-effort
 * reads of /sys/class, /proc and power-supply nodes. Anything the system
 * does not expose stays null — never fabricated.
 */
class RefurbDataSource(
    private val context: Context,
    private val batteryDataSource: BatteryDataSource
) {
    private val app = context.applicationContext
    private val pm: PackageManager get() = app.packageManager

    fun collect(): RefurbSignals {
        val battery = batteryDataSource.snapshot()
        val panelRaw = HardwareIdParsers.panelBlob { readText(it) }
        val procInput = readText("/proc/bus/input/devices")
        val touchNames = (
            HardwareIdParsers.parseTouchDevices(procInput ?: "") +
                inputDeviceTouchControllers()
            ).distinct()
        return RefurbSignals(
            touchControllers = touchNames,
            panelVendor = HardwareIdParsers.identifyVendor(panelRaw),
            panelRaw = panelRaw.ifBlank { null },
            batteryCycles = readInt("/sys/class/power_supply/battery/cycle_count")
                ?.takeIf { it in 0..100_000 },
            batteryHealthKey = battery.healthKey,
            remainingMah = battery.remainingMah,
            chargePercent = battery.percent,
            batteryVoltageMv = battery.voltageMv,
            batteryTempC = battery.temperatureCelsius,
            cameraCount = cameraCount(),
            cameraOisSupported = oisSupported(),
            usbState = usbState(),
            chargerType = chargerType(),
            fingerprintSupported = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT),
            microphone = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
            speaker = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT),
            vibratorCaps = vibratorCaps(),
            nfcPresent = runCatching {
                (app.getSystemService(Context.NFC_SERVICE) as? NfcManager)
                    ?.defaultAdapter != null
            }.getOrNull(),
            physicalSizeInches = physicalSize()
        )
    }

    private fun inputDeviceTouchControllers(): List<String> = runCatching {
        InputDevice.getDeviceIds().toList().mapNotNull { id ->
            val d = InputDevice.getDevice(id) ?: return@mapNotNull null
            val isTouch = (d.sources and InputDevice.SOURCE_TOUCHSCREEN) ==
                InputDevice.SOURCE_TOUCHSCREEN ||
                d.name.contains("touch", ignoreCase = true)
            if (!isTouch) return@mapNotNull null
            val vendor = d.vendorId.takeIf { it > 0 }?.let { String.format("%04x", it) }
            val product = d.productId.takeIf { it > 0 }?.let { String.format("%04x", it) }
            buildString {
                append(d.name)
                if (vendor != null && product != null) append(" (vid:$vendor pid:$product)")
            }
        }.distinct()
    }.getOrDefault(emptyList())

    private fun cameraCount(): Int? = runCatching {
        val cm = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cm.cameraIdList.size
    }.getOrNull()

    private fun oisSupported(): Boolean? = runCatching {
        val cm = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cm.cameraIdList.any { id ->
            val c = cm.getCameraCharacteristics(id)
            val facing = c.get(CameraCharacteristics.LENS_FACING)
            val back = facing == CameraCharacteristics.LENS_FACING_BACK
            val reqKeys = c.availableCaptureRequestKeys
            back && reqKeys.any { it.name == "LENS_OPTICAL_STABILIZATION_MODE" }
        }
    }.getOrNull()

    private fun physicalSize(): Double? = runCatching {
        val m = app.resources.displayMetrics
        CalcUtils.screenSizeInches(m.widthPixels, m.heightPixels, m.densityDpi)
    }.getOrNull()

    private fun vibratorCaps(): String? = runCatching {
        val v = if (Build.VERSION.SDK_INT >= 31) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!v.hasVibrator()) return "no vibrator"
        val amp = if (Build.VERSION.SDK_INT >= 26) v.hasAmplitudeControl() else false
        buildList {
            add("amplitude=${if (amp) "yes" else "no"}")
            if (Build.VERSION.SDK_INT >= 30) {
                val effects = listOf(0, 1, 2, 3, 4, 5, 6)
                val supported = v.areAllEffectsSupported(*effects.toIntArray())
                add("effects=${if (supported) 7 else 0}/7")
            }
        }.joinToString(" ")
    }.getOrNull()

    private fun chargerType(): String? {
        val candidates = listOf(
            "/sys/class/power_supply/battery/charge_type",
            "/sys/class/power_supply/battery/charger_type",
            "/sys/class/power_supply/usb/type"
        )
        for (path in candidates) {
            val t = readText(path)?.lines()?.firstOrNull()?.trim()
            if (!t.isNullOrEmpty() && !t.equals("Unknown", ignoreCase = true)) return t
        }
        return null
    }

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
