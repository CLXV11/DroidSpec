package com.droidspec

import com.droidspec.domain.benchmark.BenchmarkEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BenchmarkScoreTest {
    @Test
    fun scoreIsWeightedSum() {
        val expected = ((10_000_000L / 20_000_000.0) * 40.0 +
            (2000.0 / 4000.0) * 30.0 +
            (200.0 / 400.0) * 30.0).toInt()
        assertEquals(expected, BenchmarkEngine.internalScore(10_000_000L, 2000.0, 200.0))
    }

    @Test
    fun scoreIsCappedAt1000() {
        assertEquals(1000, BenchmarkEngine.internalScore(Long.MAX_VALUE / 4, 1e9, 1e9))
    }

    @Test
    fun scoreIsNonNegative() {
        assertTrue(BenchmarkEngine.internalScore(0, 0.0, 0.0) >= 0)
    }
}
