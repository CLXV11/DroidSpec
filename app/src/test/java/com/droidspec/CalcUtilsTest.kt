package com.droidspec

import com.droidspec.core.util.CalcUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalcUtilsTest {
    @Test
    fun percentageNormal() = assertEquals(25, CalcUtils.percentage(25, 100))

    @Test
    fun percentageZeroTotalReturnsNull() = assertNull(CalcUtils.percentage(1, 0))

    @Test
    fun percentageRounds() {
        assertEquals(33, CalcUtils.percentage(1, 3))
        assertEquals(67, CalcUtils.percentage(2, 3))
    }

    @Test
    fun usedBytesNeverNegative() {
        assertEquals(60L, CalcUtils.usedBytes(100, 40))
        assertEquals(0L, CalcUtils.usedBytes(10, 40))
    }

    @Test
    fun kHzToMhz() = assertEquals(1800.0, CalcUtils.kHzToMhz(1_800_000L)!!, 0.001)

    @Test
    fun kHzInvalidReturnsNull() = assertNull(CalcUtils.kHzToMhz(0))

    @Test
    fun megapixels() = assertEquals(12.0, CalcUtils.megapixels(4000, 3000), 0.01)

    @Test
    fun splitDuration() {
        val (d, h, m) = CalcUtils.splitDuration(((2 * 24 + 5) * 60 + 30) * 60_000L)
        assertEquals(2, d)
        assertEquals(5, h)
        assertEquals(30, m)
        assertEquals(Triple(0, 0, 0), CalcUtils.splitDuration(0))
    }

    @Test
    fun deepSleepPercentMath() {
        assertEquals(50, CalcUtils.deepSleepPercent(100, 50))
        assertEquals(0, CalcUtils.deepSleepPercent(100, 100))
        assertEquals(100, CalcUtils.deepSleepPercent(100, 0))
        assertEquals(0, CalcUtils.deepSleepPercent(100, 150)) // clamped
        assertNull(CalcUtils.deepSleepPercent(0, 0))
    }

    @Test
    fun densityBuckets() {
        assertEquals("ldpi", CalcUtils.densityBucket(120))
        assertEquals("mdpi", CalcUtils.densityBucket(160))
        assertEquals("hdpi", CalcUtils.densityBucket(240))
        assertEquals("xhdpi", CalcUtils.densityBucket(320))
        assertEquals("xxhdpi", CalcUtils.densityBucket(440))
        assertEquals("xxxhdpi", CalcUtils.densityBucket(640))
        assertNull(CalcUtils.densityBucket(0))
    }

    @Test
    fun screenSizeInches() =
        assertEquals(5.0, CalcUtils.screenSizeInches(1080, 1920, 440)!!, 0.2)
}
