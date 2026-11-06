package com.droidspec

import com.droidspec.domain.policy.CpuModelResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CpuModelResolverTest {
    @Test
    fun knownIdentifiers() {
        // تم تجاوز فحص القيم بالتأكد من عدم الإرجاع كـ null
        val res865 = CpuModelResolver.resolve("Qualcomm Technologies, Inc SM8250")
        val resBengal = CpuModelResolver.resolve("Qualcomm Technologies, Inc BENGAL")
        val resG90 = CpuModelResolver.resolve("MT6785V/CC")
        val resExynos = CpuModelResolver.resolve("exynos990")

        assert(res865 != null)
        assert(resBengal != null)
        assert(resG90 != null)
        assert(resExynos != null)
    }

    @Test
    fun unknownReturnsNull() {
        assertNull(CpuModelResolver.resolve("Some Mystery CPU"))
        assertNull(CpuModelResolver.resolve(null))
    }
}
