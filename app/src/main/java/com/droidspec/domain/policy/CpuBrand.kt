package com.droidspec.domain.policy

/**
 * Processor brand families. Detection is purely keyword-based on the CPU name
 * that the kernel exposes — never inferred from device model alone.
 */
enum class CpuBrand(val displayName: String) {
    SNAPDRAGON("Snapdragon"),
    MEDIATEK("MediaTek"),
    EXYNOS("Exynos"),
    KIRIN("Kirin"),
    TENSOR("Tensor"),
    UNISOC("UNISOC"),
    APPLE_A("Apple A-series"),
    APPLE_M("Apple M-series"),
    GENERIC("")
}

object CpuBrandDetector {
    fun detect(cpuName: String?): CpuBrand {
        if (cpuName.isNullOrBlank()) return CpuBrand.GENERIC
        val n = cpuName.lowercase()
        return when {
            "snapdragon" in n || "qualcomm" in n || "msm" in n || "apq" in n ||
                n.contains(Regex("""\bsm[678]\d{3}""")) -> CpuBrand.SNAPDRAGON
            "mediatek" in n || "helio" in n || "dimensity" in n ||
                n.contains(Regex("""\bmt\d{4}""")) -> CpuBrand.MEDIATEK
            "exynos" in n -> CpuBrand.EXYNOS
            "kirin" in n || n.contains(Regex("""\bhi\d{4}""")) -> CpuBrand.KIRIN
            "tensor" in n -> CpuBrand.TENSOR
            "unisoc" in n || "spreadtrum" in n || "tiger" in n ||
                n.contains(Regex("""\bsc[789]\d{3}""")) -> CpuBrand.UNISOC
            "apple" in n -> if (n.contains(Regex("""\bm\d"""))) CpuBrand.APPLE_M else CpuBrand.APPLE_A
            else -> CpuBrand.GENERIC
        }
    }
}
