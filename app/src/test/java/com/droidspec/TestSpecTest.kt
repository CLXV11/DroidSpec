package com.droidspec

import com.droidspec.domain.policy.TestSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestSpecTest {
    @Test
    fun screenTestHasFiveColors() = assertEquals(5, TestSpec.screenTestColors.size)

    @Test
    fun colorsAreOpaqueAndDistinct() {
        assertTrue(TestSpec.screenTestColors.all { it.toLong() shr 24 == 0xFFL })
        assertEquals(TestSpec.screenTestColors.size, TestSpec.screenTestColors.distinct().size)
    }

    @Test
    fun durationsPositive() {
        assertTrue(TestSpec.FPS_MEASURE_MS > 0)
        assertTrue(TestSpec.VIBRATE_MS > 0)
    }
}
