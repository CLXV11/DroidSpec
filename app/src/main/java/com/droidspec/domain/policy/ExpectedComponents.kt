package com.droidspec.domain.policy

/**
 * Clearly-labeled local database of expected component configurations per
 * device codename (cameras / fingerprint / NFC). Used ONLY for
 * expected-vs-actual comparison in the refurbishment check. Unknown
 * codenames return null and every value is presented in the UI as a
 * database-derived estimate.
 */
data class ExpectedSpec(
    val cameras: Int?,
    val fingerprint: Boolean?,
    val nfc: Boolean?
)

object ExpectedComponents {
    private val db: Map<String, ExpectedSpec> = mapOf(
        "ginkgo" to ExpectedSpec(4, true, false),          // Redmi Note 8
        "willow" to ExpectedSpec(4, true, false),          // Redmi Note 8T
        "violet" to ExpectedSpec(3, true, false),          // Redmi Note 7 Pro
        "lavender" to ExpectedSpec(3, true, false),        // Redmi Note 7
        "mido" to ExpectedSpec(2, true, false),            // Redmi Note 4
        "whyred" to ExpectedSpec(3, true, false),          // Redmi Note 5
        "tissot" to ExpectedSpec(2, true, false),          // Mi A1
        "jasmine_sprout" to ExpectedSpec(2, true, false),  // Mi A2
        "surya" to ExpectedSpec(5, true, true),            // POCO X3 NFC
        "vayu" to ExpectedSpec(5, true, true),             // POCO X3 Pro
        "sweet" to ExpectedSpec(4, true, null),            // Redmi Note 10 Pro (NFC varies)
        "mojito" to ExpectedSpec(4, true, null),           // Redmi Note 10
        "santoni" to ExpectedSpec(2, false, false),        // Redmi 4X
        "rolex" to ExpectedSpec(2, false, false)           // Redmi 4A
    )

    fun forDevice(codename: String?): ExpectedSpec? =
        codename?.lowercase()?.let { db[it] }
}
