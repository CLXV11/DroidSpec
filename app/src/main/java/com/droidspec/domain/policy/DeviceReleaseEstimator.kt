package com.droidspec.domain.policy

/**
 * Clearly-labeled local database of well-known device codenames → market
 * release year. Android exposes NO official API for release year, so this is
 * an ESTIMATE and is always labeled as such in the UI. Unknown codenames
 * return null (shown as "Not available") — never fabricated.
 */
object DeviceReleaseEstimator {
    private val known = mapOf(
        "ginkgo" to 2019, "willow" to 2019, "violet" to 2019,
        "laurel_sprout" to 2019, "davinci" to 2019, "raphael" to 2019,
        "tissot" to 2017, "jasmine_sprout" to 2018, "whyred" to 2018,
        "mido" to 2017, "surya" to 2020, "vayu" to 2021,
        "mojito" to 2021, "sweet" to 2021, "apollo" to 2020,
        "pine" to 2019, "olive" to 2019, "phoenix" to 2020,
        "cepheus" to 2019, "crux" to 2019
    )

    fun estimate(codename: String?): Int? =
        codename?.lowercase()?.let { known[it] }
}
