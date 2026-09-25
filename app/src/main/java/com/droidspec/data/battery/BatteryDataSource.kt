package com.droidspec.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.droidspec.domain.model.BatteryInfo

class BatteryDataSource(context: Context) {
    private val app = context.applicationContext
    private val batteryManager =
        app.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val powerManager =
        app.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun snapshot(): BatteryInfo = runCatching {
        val intent: Intent? =
            app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else null

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it >= 0 }?.div(10f)
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.ifBlank { null }
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)?.takeIf { it >= 0 }

        // Current now: microamperes -> mA. Not all fuel gauges report it.
        val current = runCatching {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        }.getOrNull()?.takeIf { it != Int.MIN_VALUE }?.let { it / 1000 }

        val capacity = runCatching {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }.getOrNull()?.takeIf { it in 0..100 }

        val remainingMah = runCatching {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        }.getOrNull()?.takeIf { it != Int.MIN_VALUE }?.let { it / 1000 }

        val charging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false
            else -> null
        }

        BatteryInfo(
            percent = percent,
            statusKey = BatteryMappers.mapStatus(status),
            charging = charging,
            sourceKey = BatteryMappers.mapPlugged(plugged),
            temperatureCelsius = temperature,
            voltageMv = voltage,
            currentMa = current,
            technology = technology,
            healthKey = health?.let { BatteryMappers.mapHealth(it) },
            capacityPercent = capacity,
            batterySaverOn = runCatching { powerManager.isPowerSaveMode }.getOrNull(),
            remainingMah = remainingMah
        )
    }.getOrElse {
        BatteryInfo(null, null, null, null, null, null, null, null, null, null, null, null)
    }
}
