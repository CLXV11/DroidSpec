package com.droidspec.data.thermal

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.model.ThermalThresholds
import com.droidspec.domain.model.ThermalZone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

/**
 * Only temperatures that Android/the kernel actually expose are reported:
 *  - battery temperature (sticky ACTION_BATTERY_CHANGED intent)
 *  - ambient temperature sensor, when present
 *  - /sys/class/thermal zones, best effort and clearly labeled as such
 */
class ThermalDataSource(context: Context) {
    private val app = context.applicationContext

    fun batteryTemperature(): Float? = runCatching {
        val intent: Intent? = app.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it >= 0 }?.div(10f)
    }.getOrNull()

    fun thermalZones(): List<ThermalZone> = runCatching {
        val dir = File("/sys/class/thermal")
        val zones = dir.listFiles { f -> f.name.startsWith("thermal_zone") }
            ?: return emptyList()
        zones.sortedBy { it.name }.mapNotNull { zone ->
            val tempRaw = File(zone, "temp").readText().trim().toFloatOrNull() ?: return@mapNotNull null
            val millidegree = kotlin.math.abs(tempRaw) > 1000f
            val type = File(zone, "type").takeIf { it.canRead() }
                ?.readText()?.trim()?.ifBlank { null }
            ThermalZone(
                name = zone.name,
                type = type,
                temperatureCelsius = if (millidegree) tempRaw / 1000f else tempRaw
            )
        }
    }.getOrDefault(emptyList())

    /** Emits ambient temperature while collected; unsubscribes on close. */
    fun observeAmbientTemperature(): Flow<Float?> = callbackFlow {
        val sensorManager =
            app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (sensor == null) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values.firstOrNull())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    companion object {
        fun severityFor(maxTemp: Float?, thresholds: ThermalThresholds) =
            com.droidspec.domain.policy.ThermalPolicy.classify(maxTemp, thresholds)

        fun buildSnapshot(
            battery: Float?,
            ambient: Float?,
            zones: List<ThermalZone>,
            thresholds: ThermalThresholds
        ): ThermalSnapshot {
            val maxTemp = listOfNotNull(
                battery, ambient, zones.maxOfOrNull { it.temperatureCelsius }
            ).maxOrNull()
            return ThermalSnapshot(
                batteryCelsius = battery,
                ambientCelsius = ambient,
                zones = zones,
                severity = severityFor(maxTemp, thresholds),
                thresholds = thresholds
            )
        }
    }
}
