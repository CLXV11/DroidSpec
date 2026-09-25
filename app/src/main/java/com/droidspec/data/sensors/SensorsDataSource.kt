package com.droidspec.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.droidspec.domain.model.SensorSpec
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorsDataSource(context: Context) {
    private val app = context.applicationContext

    fun snapshot(): List<SensorSpec> = runCatching {
        val sm = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.getSensorList(Sensor.TYPE_ALL).map { s ->
            SensorSpec(
                type = s.type,
                name = s.name,
                vendor = s.vendor?.ifBlank { null },
                version = s.version,
                powerMa = s.power,
                resolution = s.resolution,
                maxRange = s.maximumRange,
                wakeUp = s.isWakeUpSensor
            )
        }
    }.getOrDefault(emptyList())

    /** Live values for one sensor type. Listener is removed when flow closes. */
    fun observeSensorValues(sensorType: Int): Flow<FloatArray> = callbackFlow {
        val sm = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = runCatching { sm.getDefaultSensor(sensorType) }.getOrNull()
        if (sensor == null) {
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values.copyOf())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sm.unregisterListener(listener) }
    }
}
