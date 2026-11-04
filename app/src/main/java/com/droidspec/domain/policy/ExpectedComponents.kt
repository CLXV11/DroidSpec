package com.droidspec.domain.policy

/**
 * Clearly-labeled LOCAL reference database: expected factory configuration
 * per device codename. Values are compiled from public device documentation;
 * null means "no reliable reference" — the UI then shows "no reference"
 * instead of guessing. A server-side variant could replace this later.
 */
data class ExpectedSpec(
    val socName: String?,
    val cameras: Int?,
    val fingerprint: Boolean?,
    val nfc: Boolean?,
    val batteryDesignMah: Int?,
    val panelVendors: List<String>?,
    val touchControllers: List<String>?
)

object ExpectedComponents {
    private val db: Map<String, ExpectedSpec> = mapOf(
        "ginkgo" to ExpectedSpec("Snapdragon 665", 4, true, false, 4000, null, null),
        "willow" to ExpectedSpec("Snapdragon 665", 4, true, true, 4000, null, null),
        "violet" to ExpectedSpec("Snapdragon 675", 3, true, false, 4000, null, null),
        "lavender" to ExpectedSpec("Snapdragon 660", 3, true, false, 4000, null, null),
        "mido" to ExpectedSpec("Snapdragon 625", 2, true, false, 4100, null, null),
        "whyred" to ExpectedSpec("Snapdragon 636", 3, true, false, 4000, null, null),
        "tissot" to ExpectedSpec("Snapdragon 625", 2, true, false, 3080, null, null),
        "jasmine_sprout" to ExpectedSpec("Snapdragon 660", 2, true, false, 3000, null, null),
        "surya" to ExpectedSpec("Snapdragon 732G", 5, true, true, 5160, null, null),
        "vayu" to ExpectedSpec("Snapdragon 860", 5, true, true, 5160, null, null),
        "sweet" to ExpectedSpec("Snapdragon 732G", 4, true, null, 5020, null, null),
        "mojito" to ExpectedSpec("Snapdragon 678", 4, true, null, 5000, null, null),
        "santoni" to ExpectedSpec("Snapdragon 435", 2, false, false, 4100, null, null),
        "rolex" to ExpectedSpec("Snapdragon 425", 2, false, false, 3120, null, null)
    )

    fun forDevice(codename: String?): ExpectedSpec? =
        codename?.lowercase()?.let { db[it] }
}
