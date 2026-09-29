package com.droidspec

import com.droidspec.domain.benchmark.AdvancedBench
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedBenchTest {
    @Test
    fun memoryLatencyIsPlausible() {
        val ns = AdvancedBench.memoryLatencyNs()
        assertTrue("latency $ns ns out of range", ns in 2.0..2000.0)
    }

    @Test
    fun iopsResultMath() {
        val r = AdvancedBench.IopsResult(120.0, 340.0)
        assertEquals(120.0, r.writeIops, 0.001)
        assertEquals(340.0, r.readIops, 0.001)
    }

    @Test
    fun throttleDropMath() {
        // drop = (start-min)*100/start, clamped at 0
        val drop = (((2400.0 - 1500.0).coerceAtLeast(0.0)) * 100 / 2400.0).toInt()
        assertEquals(37, drop)
    }
}
