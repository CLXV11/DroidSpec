package com.droidspec

import com.droidspec.domain.model.ThermalSeverity
import com.droidspec.domain.model.ThermalThresholds
import com.droidspec.domain.policy.ThermalPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ThermalPolicyTest {
    private val thresholds = ThermalThresholds(warmC = 38f, hotC = 45f, criticalC = 52f)

    @Test
    fun nullIsNormal() =
        assertEquals(ThermalSeverity.NORMAL, ThermalPolicy.classify(null, thresholds))

    @Test
    fun boundaries() {
        assertEquals(ThermalSeverity.NORMAL, ThermalPolicy.classify(37.9f, thresholds))
        assertEquals(ThermalSeverity.WARM, ThermalPolicy.classify(38f, thresholds))
        assertEquals(ThermalSeverity.HOT, ThermalPolicy.classify(45f, thresholds))
        assertEquals(ThermalSeverity.CRITICAL, ThermalPolicy.classify(52f, thresholds))
        assertEquals(ThermalSeverity.CRITICAL, ThermalPolicy.classify(80f, thresholds))
    }
}
