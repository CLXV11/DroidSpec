package com.droidspec.domain.policy

/**
 * Maps exposed CPU identifiers (Qualcomm platform codenames, MediaTek chip
 * IDs, Exynos/Kirin/Tensor board names) to marketing names.
 *
 * This is a LOCAL, clearly-labeled database — the kernel exposes codenames,
 * not marketing names. Unknown identifiers return null (never fabricated).
 */
object CpuModelResolver {
    private val table: List<Pair<Regex, String>> = listOf(
        Regex("""sm8650""") to "Qualcomm Snapdragon 8 Gen 3",
        Regex("""sm8550""") to "Qualcomm Snapdragon 8 Gen 2",
        Regex("""sm8450""") to "Qualcomm Snapdragon 8 Gen 1",
        Regex("""sm8350""") to "Qualcomm Snapdragon 888",
        Regex("""sm8250""") to "Qualcomm Snapdragon 865",
        Regex("""sm8150""") to "Qualcomm Snapdragon 855",
        Regex("""sdm855""") to "Qualcomm Snapdragon 855",
        Regex("""sdm845""") to "Qualcomm Snapdragon 845",
        Regex("""msm8998""") to "Qualcomm Snapdragon 835",
        Regex("""msm8996""") to "Qualcomm Snapdragon 820",
        Regex("""trinket""") to "Qualcomm Snapdragon 665",
        Regex("""bengal""") to "Qualcomm Snapdragon 662",
        Regex("""msm8953""") to "Qualcomm Snapdragon 625",
        Regex("""msm8937""") to "Qualcomm Snapdragon 430",
        Regex("""mt6983""") to "MediaTek Dimensity 9000",
        Regex("""mt6893""") to "MediaTek Dimensity 1200",
        Regex("""mt6877""") to "MediaTek Dimensity 900",
        Regex("""mt6833""") to "MediaTek Dimensity 810",
        Regex("""mt6781""") to "MediaTek Helio G96",
        Regex("""mt6785""") to "MediaTek Helio G90",
        Regex("""mt6768""") to "MediaTek Helio P65",
        Regex("""mt6765""") to "MediaTek Helio P35",
        Regex("""exynos2200""") to "Samsung Exynos 2200",
        Regex("""exynos2100""") to "Samsung Exynos 2100",
        Regex("""exynos990""") to "Samsung Exynos 990",
        Regex("""exynos9820""") to "Samsung Exynos 9820",
        Regex("""exynos9611""") to "Samsung Exynos 9611",
        Regex("""exynos7904""") to "Samsung Exynos 7904",
        Regex("""kirin990""") to "Kirin 990",
        Regex("""kirin980""") to "Kirin 980",
        Regex("""kirin810""") to "Kirin 810",
        Regex("""hi6250""") to "Kirin 650 series",
        Regex("""gs101""") to "Google Tensor G1",
        Regex("""gs201""") to "Google Tensor G2",
        Regex("""gs301""") to "Google Tensor G3"
    )

    /** Marketing name from the exposed identifier, or null when unknown. */
    fun resolve(rawName: String?): String? {
        if (rawName.isNullOrBlank()) return null
        val n = rawName.lowercase()
        return table.firstOrNull { it.first.containsMatchIn(n) }?.second
    }

    /**
     * Context-aware resolution: an exact per-device factory SoC name (from
     * the labeled reference DB) wins over family-level codename guesses —
     * so "BENGAL" on a ginkgo resolves to "Snapdragon 665", not "662".
     */
    fun resolveSmart(rawName: String?, codename: String?): String? =
        ExpectedComponents.forDevice(codename)?.socName ?: resolve(rawName)
}
