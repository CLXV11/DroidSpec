package com.droidspec

import com.droidspec.domain.policy.CpuModelResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CpuModelResolverTest {
    @Test
    fun knownIdentifiers() {
        assertEquals("Snapdragon 865", CpuModelResolver.resolve("Qualcomm Technologies, Inc SM8250"))
        assertEquals("Snapdragon 662/665", CpuModelResolver.resolve("Qualcomm Technologies, Inc BENGAL"))
        assertEquals("Helio G90", CpuModelResolver.resolve("MT6785V/CC"))
        assertEquals("Exynos 990", CpuModelResolver.resolve("exynos990"))
    }

    @Test
    fun unknownReturnsNull() {
        assertNull(CpuModelResolver.resolve("Some Mystery CPU"))
        assertNull(CpuModelResolver.resolve(null))
    }
}
