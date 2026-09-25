package com.droidspec

import android.os.BatteryManager
import com.droidspec.data.battery.BatteryMappers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryMappersTest {
    @Test
    fun healthMapping() {
        assertEquals("good", BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_GOOD))
        assertEquals("overheat", BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_OVERHEAT))
        assertEquals("dead", BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_DEAD))
        assertEquals("over_voltage",
            BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE))
        assertEquals("unspecified",
            BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE))
        assertEquals("cold", BatteryMappers.mapHealth(BatteryManager.BATTERY_HEALTH_COLD))
        assertEquals("unknown", BatteryMappers.mapHealth(-1))
    }

    @Test
    fun statusMapping() {
        assertEquals("charging", BatteryMappers.mapStatus(BatteryManager.BATTERY_STATUS_CHARGING))
        assertEquals("discharging",
            BatteryMappers.mapStatus(BatteryManager.BATTERY_STATUS_DISCHARGING))
        assertEquals("full", BatteryMappers.mapStatus(BatteryManager.BATTERY_STATUS_FULL))
        assertEquals("not_charging",
            BatteryMappers.mapStatus(BatteryManager.BATTERY_STATUS_NOT_CHARGING))
        assertEquals("unknown", BatteryMappers.mapStatus(-1))
    }

    @Test
    fun pluggedMapping() {
        assertNull(BatteryMappers.mapPlugged(0))
        assertEquals("usb", BatteryMappers.mapPlugged(BatteryManager.BATTERY_PLUGGED_USB))
        assertEquals("ac", BatteryMappers.mapPlugged(BatteryManager.BATTERY_PLUGGED_AC))
        assertEquals("wireless",
            BatteryMappers.mapPlugged(BatteryManager.BATTERY_PLUGGED_WIRELESS))
    }
}
