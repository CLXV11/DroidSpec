package com.droidspec

import com.droidspec.domain.policy.CpuBrand
import com.droidspec.domain.policy.CpuBrandDetector
import org.junit.Assert.assertEquals
import org.junit.Test

class CpuBrandDetectorTest {
    @Test
    fun snapdragonNames() {
        assertEquals(CpuBrand.SNAPDRAGON, CpuBrandDetector.detect("Qualcomm Snapdragon 665"))
        assertEquals(CpuBrand.SNAPDRAGON, CpuBrandDetector.detect("SM8250"))
    }

    @Test
    fun mediatekNames() {
        assertEquals(CpuBrand.MEDIATEK, CpuBrandDetector.detect("MediaTek Helio G85"))
        assertEquals(CpuBrand.MEDIATEK, CpuBrandDetector.detect("Dimensity 8100"))
        assertEquals(CpuBrand.MEDIATEK, CpuBrandDetector.detect("MT6785"))
    }

    @Test
    fun otherBrands() {
        assertEquals(CpuBrand.EXYNOS, CpuBrandDetector.detect("Exynos 990"))
        assertEquals(CpuBrand.KIRIN, CpuBrandDetector.detect("Kirin 980"))
        assertEquals(CpuBrand.TENSOR, CpuBrandDetector.detect("Google Tensor G2"))
        assertEquals(CpuBrand.UNISOC, CpuBrandDetector.detect("UNISOC SC9863A"))
    }

    @Test
    fun appleNames() {
        assertEquals(CpuBrand.APPLE_A, CpuBrandDetector.detect("Apple A16 Bionic"))
        assertEquals(CpuBrand.APPLE_M, CpuBrandDetector.detect("Apple M2"))
    }

    @Test
    fun unknownAndNull() {
        assertEquals(CpuBrand.GENERIC, CpuBrandDetector.detect(null))
        assertEquals(CpuBrand.GENERIC, CpuBrandDetector.detect(""))
        assertEquals(CpuBrand.GENERIC, CpuBrandDetector.detect("Some Mystery CPU"))
    }
}
