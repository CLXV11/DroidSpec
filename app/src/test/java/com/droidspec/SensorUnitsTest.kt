package com.droidspec

import android.hardware.Sensor
import com.droidspec.domain.policy.SensorUnits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorUnitsTest {
    @Test
    fun knownUnits() {
        assertEquals("m/s²", SensorUnits.unitFor(Sensor.TYPE_ACCELEROMETER))
        assertEquals("µT", SensorUnits.unitFor(Sensor.TYPE_MAGNETIC_FIELD))
        assertEquals("lux", SensorUnits.unitFor(Sensor.TYPE_LIGHT))
        assertEquals("hPa", SensorUnits.unitFor(Sensor.TYPE_PRESSURE))
        assertEquals("°C", SensorUnits.unitFor(Sensor.TYPE_AMBIENT_TEMPERATURE))
        assertEquals("%", SensorUnits.unitFor(Sensor.TYPE_RELATIVE_HUMIDITY))
        assertEquals("cm", SensorUnits.unitFor(Sensor.TYPE_PROXIMITY))
        assertEquals("rad/s", SensorUnits.unitFor(Sensor.TYPE_GYROSCOPE))
    }

    @Test
    fun unknownIsNull() {
        assertNull(SensorUnits.unitFor(Sensor.TYPE_ROTATION_VECTOR))
        assertNull(SensorUnits.unitFor(-1))
    }
}
