package com.droidspec.domain.policy

import android.hardware.Sensor

/** SI unit labels for live sensor readings. Pure mapping, unit-testable. */
object SensorUnits {
    fun unitFor(type: Int): String? = when (type) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GRAVITY,
        Sensor.TYPE_LINEAR_ACCELERATION -> "m/s²"
        Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "rad/s"
        Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "µT"
        Sensor.TYPE_LIGHT -> "lux"
        Sensor.TYPE_PRESSURE -> "hPa"
        Sensor.TYPE_TEMPERATURE, Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
        Sensor.TYPE_PROXIMITY -> "cm"
        Sensor.TYPE_STEP_COUNTER -> "steps"
        else -> null
    }
}
