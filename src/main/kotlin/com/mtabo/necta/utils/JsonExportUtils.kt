package necta.utils


import com.mtabo.necta.core.UrlBuilder
import com.mtabo.necta.models.District
import com.mtabo.necta.models.ExamType
import com.mtabo.necta.models.Region
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.mtabo.necta.parser.parseDistricts
import com.mtabo.necta.parser.parseRegions
import com.mtabo.necta.core.FetchResult
import com.mtabo.necta.core.JsoupClient
import java.io.File

@Serializable
data class JsonRegionData(
    val code: String,
    val name: String,
    val validFrom: Int,
    var validUntil: Int? = null
)

@Serializable
data class JsonDistrictData(
    val code: String,
    val name: String,
    val validFrom: Int,
    var validUntil: Int? = null
)

/**
 * Function for scraping, parsing, and processing regions and districts to a json file.
 *
 */
suspend fun collectRegionsAndDistrictsWithHistory(
    startYear: Int = 2013,
    endYear: Int = 2025
): Pair<List<JsonRegionData>, List<JsonDistrictData>> {

    val regions = mutableListOf<JsonRegionData>()
    val districts = mutableListOf<JsonDistrictData>()

    val activeRegions = mutableMapOf<String, JsonRegionData>()
    val activeDistricts = mutableMapOf<String, JsonDistrictData>()

    for (year in startYear..endYear) {

        println("\n=== Processing $year ===")

        val seenRegions = mutableSetOf<String>()
        val seenDistricts = mutableSetOf<String>()

        val fetchedRegions = fetchRegionsForYear(year) ?: continue

        // ---------- REGIONS ----------
        updateRegionHistory(
            year,
            fetchedRegions,
            regions,
            activeRegions,
            seenRegions
        )

        // ---------- DISTRICTS ----------
        for (region in fetchedRegions) {
            val fetchedDistricts = fetchDistrictsForYear(year, region.code) ?: continue

            updateDistrictHistory(
                year,
                fetchedDistricts,
                districts,
                activeDistricts,
                seenDistricts
            )
        }

        closeMissing(activeRegions, seenRegions, year) { println("✖ Region closed: $it") }
        closeMissing(activeDistricts, seenDistricts, year) { println("✖ District closed: $it") }
    }

    return regions to districts
}

suspend fun fetchRegionsForYear(year: Int): List<Region>? {
    val url = UrlBuilder.buildRegionListUrl(ExamType.PSLE, year) ?: return null

    return when (val result = JsoupClient.fetchDocument(url)) {
        is FetchResult.Success -> parseRegions(result.data)
        is FetchResult.Error -> {
            println("❌ Failed to fetch regions for $year: $result")
            null
        }
    }
}

suspend fun fetchDistrictsForYear(year: Int, regionCode: String): List<District>? {
    val url = UrlBuilder.buildDistrictListUrl(ExamType.PSLE, year, regionCode) ?: return null

    return when (val result = JsoupClient.fetchDocument(url)) {
        is FetchResult.Success -> parseDistricts(result.data)
        is FetchResult.Error -> {
            println("❌ Failed to fetch districts for $year / $regionCode")
            null
        }
    }
}

fun updateRegionHistory(
    year: Int,
    fetched: List<Region>,
    output: MutableList<JsonRegionData>,
    active: MutableMap<String, JsonRegionData>,
    seen: MutableSet<String>
) {
    for (region in fetched) {

        seen.add(region.code)
        val existing = active[region.code]

        when {
            existing == null -> {
                val newRegion = JsonRegionData(region.code, region.name, year, null)
                output.add(newRegion)
                active[region.code] = newRegion
                println("✔ Region added: ${region.name}")
            }

            existing.name != region.name -> {
                existing.validUntil = year

                val newRegion = JsonRegionData(region.code, region.name, year, null)
                output.add(newRegion)
                active[region.code] = newRegion

                println("↻ Region renamed: ${existing.name} → ${region.name}")
            }
        }
    }
}

fun updateDistrictHistory(
    year: Int,
    fetched: List<District>,
    output: MutableList<JsonDistrictData>,
    active: MutableMap<String, JsonDistrictData>,
    seen: MutableSet<String>
) {
    for (district in fetched) {

        seen.add(district.code)
        val existing = active[district.code]

        when {
            existing == null -> {
                val newDistrict = JsonDistrictData(district.code, district.name, year, null)
                output.add(newDistrict)
                active[district.code] = newDistrict
                println("✔ District added: ${district.name}")
            }

            existing.name != district.name -> {
                existing.validUntil = year

                val newDistrict = JsonDistrictData(district.code, district.name, year, null)
                output.add(newDistrict)
                active[district.code] = newDistrict

                println("↻ District renamed: ${existing.name} → ${district.name}")
            }
        }
    }
}

fun <T : Any> closeMissing(
    active: Map<String, T>,
    seen: Set<String>,
    year: Int,
    log: (String) -> Unit
) {
    active.forEach { (code, item) ->
        val validUntilField = item::class.members.find { it.name == "validUntil" } ?: return@forEach

        val value = validUntilField.call(item) as? Int
        if (code !in seen && value == null) {
            (item as? JsonRegionData)?.validUntil = year
            (item as? JsonDistrictData)?.validUntil = year
            log(code)
        }
    }
}

fun saveRegionAndDistrictJson(
    regions: List<JsonRegionData>,
    districts: List<JsonDistrictData>
) {
    val json = Json { prettyPrint = true }

    val regionsFile = File(System.getProperty("user.home"), "regions.json")
    val districtsFile = File(System.getProperty("user.home"), "districts.json")

    regionsFile.writeText(json.encodeToString(regions))
    districtsFile.writeText(json.encodeToString(districts))

    println("\n✅ JSON saved")
    println(regionsFile.absolutePath)
    println(districtsFile.absolutePath)
}

fun main() = runBlocking {
    val (regions, districts) = collectRegionsAndDistrictsWithHistory()

    println("\nCollected:")
    println("Regions: ${regions.size}")
    println("Districts: ${districts.size}")

    saveRegionAndDistrictJson(regions, districts)
}