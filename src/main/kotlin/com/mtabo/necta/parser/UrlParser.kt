package com.mtabo.necta.parser

import com.mtabo.necta.models.ExamType
import com.mtabo.necta.parser.UrlParser.extractDistrictsListUrl
import com.mtabo.necta.parser.UrlParser.getPrimarySchoolUrl
import com.mtabo.necta.parser.UrlParser.getPrimarySchoolsListUrl
import com.mtabo.necta.parser.UrlParser.getResultEntriesUrl
import com.mtabo.necta.parser.UrlParser.getSecondarySchoolUrl
import kotlinx.coroutines.runBlocking
import com.mtabo.necta.core.FetchResult
import com.mtabo.necta.core.JsoupClient.fetchDocument
import org.jsoup.Jsoup
import java.net.URL
import kotlin.text.lowercase

object UrlParser {

    suspend fun extractSchoolResultsUrl(
        examType: ExamType,
        year: Int,
        schoolCode: String
    ): String {
        return when(examType) {
            ExamType.ACSEE, ExamType.CSEE, ExamType.FTNA ->
                getSecondarySchoolUrl(examType, year, schoolCode)

            ExamType.PSLE, ExamType.SFNA ->
                getPrimarySchoolUrl(examType, year, schoolCode)
        }
    }

    suspend fun extractSchoolListUrl(
        examType: ExamType,
        year: Int,
        districtId: String? = null
    ): String {
        return when(examType) {
            ExamType.ACSEE, ExamType.CSEE, ExamType.FTNA ->
                getSecondarySchoolsListUrl(examType, year)

            ExamType.PSLE, ExamType.SFNA ->
                getPrimarySchoolsListUrl(examType, year, districtId ?: "")
        }
    }

    /**
     * Fetches the URL of a region page for a primary exam.
     *
     * @param examType The primary exam type (PSLE or SFNA).
     * @param year The exam year.
     * @param regionCode The 2-digit region code.
     * @return The region URL, or empty string if not found or on error.
     */
    suspend fun extractDistrictsListUrl(
        examType: ExamType,
        year: Int,
        regionCode: String
    ): String {
        val regionsPageUrl = extractRegionsListUrl(examType, year)
        if (regionsPageUrl.isEmpty()) return ""

        val fetchResult = fetchDocument(regionsPageUrl)

        return when (fetchResult) {
            is FetchResult.Success -> {
                val doc = fetchResult.data

                // 🔹 Pattern differs for SFNA vs PSLE
                val regionPattern = when (examType) {
                    ExamType.SFNA -> Regex("""(results/)?reg_ps${regionCode}\.htm""", RegexOption.IGNORE_CASE)
                    else -> Regex("""(results/)?reg_${regionCode}\.htm""", RegexOption.IGNORE_CASE)
                }

                val regionLink = doc.select("a[href]").firstOrNull { element ->
                    regionPattern.containsMatchIn(element.attr("href"))
                } ?: return "" // region not found

                normalizeUrl(URL(URL(regionsPageUrl), regionLink.attr("href")).toString())
            }
            is FetchResult.Error -> {
                println("Failed to fetch regions page: $fetchResult")
                ""
            }
        }
    }

    /**
     * Returns the URL of the regions list page for a primary exam.
     *
     * @param examType The primary exam type (PSLE or SFNA).
     * @param year The exam year.
     * @return The regions list page URL.
     * @throws IllegalArgumentException if the exam type is not primary.
     */
    suspend fun extractRegionsListUrl(
        examType: ExamType,
        year: Int
    ): String {
        require(examType in listOf(ExamType.PSLE, ExamType.SFNA)) {
            "Requires Primary schools only"
        }
        return getResultEntriesUrl(examType, year)
    }

    suspend fun getSecondarySchoolUrl(
        examType: ExamType,
        year: Int,
        schoolCode: String
    ): String {
        val schoolListUrl = getSecondarySchoolsListUrl(examType, year)

        val fetchResult = fetchDocument(schoolListUrl)

        return when (fetchResult) {
            is FetchResult.Success -> {
                val doc = fetchResult.data

                // 🔹 Old broken CSEE pages
                if (examType == ExamType.CSEE && year in 2003..2009) {
                    val regex = Regex(
                        """href\s*=\s*['"]([^'"]*${Regex.escape(schoolCode)}[^'"]*)['"]""",
                        RegexOption.IGNORE_CASE
                    )
                    regex.findAll(doc.outerHtml())
                        .map { match -> normalizeUrl(URL(URL(schoolListUrl), match.groupValues[1]).toString()) }
                        .firstOrNull()
                        ?: ""
                } else {
                    // 🔹 Modern pages
                    doc.select("a[href]").firstNotNullOfOrNull { element ->
                        val href = element.attr("href")
                        if (href.lowercase().contains(schoolCode.lowercase())) {
                            normalizeUrl(URL(URL(schoolListUrl), href).toString())
                        } else null
                    }
                        ?: ""
                }
            }

            is FetchResult.Error -> {
                println("Failed to fetch school list page: $fetchResult")
                ""
            }
        }
    }

    /**
     * Fetches the URL of a specific primary school result page based on the exam type, year, and school code.
     *
     * For primary exams (PSLE or SFNA), this function:
     * 1. Extracts the district code from the first 4 digits of the school code.
     * 2. Retrieves the district-level results page URL.
     * 3. Fetches and parses the district page HTML using `fetchDocument`.
     * 4. Extracts the school-specific result link, handling old (malformed) pages and modern pages differently.
     * 5. Returns the first matching school URL, or an empty string if no match is found.
     *
     * @param examType The primary exam type (PSLE or SFNA).
     * @param year The year of the exam.
     * @param schoolCode The full school code (used to derive district and match the school link).
     * @return The URL of the school’s result page, or empty string if not found or an error occurs.
     * @throws IllegalArgumentException if the exam type is not PSLE or SFNA.
     */
    suspend fun getPrimarySchoolUrl(
        examType: ExamType,
        year: Int,
        schoolCode: String
    ): String {
        // Step 1: Extract district code from the first 4 digits of schoolCode
        val districtId = schoolCode.take(4)

        // Step 2: Get district-level results page URL
        val districtUrl = getPrimarySchoolsListUrl(examType, year, districtId)
        if (districtUrl.isEmpty()) return ""

        // Step 3: Fetch district page HTML safely using fetchDocument
        val fetchResult = fetchDocument(districtUrl)

        return when (fetchResult) {
            is FetchResult.Success -> {
                val doc = fetchResult.data

                // Step 4: Extract school-specific link
                val schoolLink = if (examType == ExamType.PSLE && year in 2013..2015) {
                    // 🔹 Handle old malformed PSLE pages
                    val regex = Regex(
                        """href\s*=\s*['"]([^'"]*${Regex.escape(schoolCode)}[^'"]*)['"]""",
                        RegexOption.IGNORE_CASE
                    )
                    regex.findAll(doc.outerHtml())
                        .map { match -> normalizeUrl(URL(URL(districtUrl), match.groupValues[1]).toString()) }
                        .firstOrNull()
                } else {
                    // 🔹 Modern pages
                    doc.select("a[href]").firstNotNullOfOrNull { element ->
                        val href = element.attr("href")
                        if (href.lowercase().contains(schoolCode.lowercase())) {
                            normalizeUrl(URL(URL(districtUrl), href).toString())
                        } else null
                    }
                }

                // Step 5: Return the found URL or empty string
                schoolLink ?: ""
            }

            is FetchResult.Error -> {
                // Failed to fetch page; log error and return empty
                println("Failed to fetch district page: $fetchResult")
                ""
            }
        }
    }

    /**
     * Returns the URL of the secondary schools list page for a given exam and year.
     *
     * @param examType The secondary exam type (CSEE, ACSEE, or FTNA).
     * @param year The exam year.
     * @return The secondary schools list page URL.
     * @throws IllegalArgumentException if the exam type is not secondary.
     */
    suspend fun getSecondarySchoolsListUrl(
        examType: ExamType,
        year: Int
    ): String {
        require(examType in listOf(ExamType.CSEE, ExamType.ACSEE, ExamType.FTNA)) {
            "Invalid exam type for secondary schools"
        }
        return getResultEntriesUrl(examType, year)
    }

    /**
     * Fetches the URL of a specific district-level page for a primary exam.
     *
     * This function:
     * 1. Normalizes the district code and derives the region code.
     * 2. Fetches the region page URL via [extractDistrictsListUrl].
     * 3. Parses the HTML using [fetchDocument] and extracts the district link.
     * 4. Handles old broken PSLE pages differently from modern pages.
     *
     * @param examType The primary exam type (PSLE or SFNA).
     * @param year The exam year.
     * @param districtCode The 4-digit district code.
     * @return The district result URL, or an empty string if not found or on error.
     */
    suspend fun getPrimarySchoolsListUrl(
        examType: ExamType,
        year: Int,
        districtCode: String
    ): String {
        val regionCode = districtCode.dropLast(2)
        val resultUrl = extractDistrictsListUrl(examType, year, regionCode)
        if (resultUrl.isEmpty()) return ""

        val fetchResult = fetchDocument(resultUrl)

        return when (fetchResult) {
            is FetchResult.Success -> {
                val doc = fetchResult.data
                val links: List<String> = if (examType == ExamType.PSLE && year in 2013..2015) {
                    // 🔹 Old broken PSLE pages may have malformed HTML
                    val regex = Regex(
                        """href\s*=\s*['"]([^'"]*${Regex.escape(districtCode)}[^'"]*)['"]""",
                        RegexOption.IGNORE_CASE
                    )
                    regex.findAll(doc.outerHtml())
                        .map { match -> normalizeUrl(URL(URL(resultUrl), match.groupValues[1]).toString()) }
                        .toList()
                } else {
                    // 🔹 Modern pages
                    val districtPattern = when (examType) {
                        ExamType.SFNA -> Regex("""distr_ps$districtCode.*\.htm""", RegexOption.IGNORE_CASE)
                        else -> Regex("""distr_$districtCode.*\.htm""", RegexOption.IGNORE_CASE)
                    }

                    doc.select("a[href]")
                        .mapNotNull { element ->
                            val href = element.attr("href")
                            if (districtPattern.containsMatchIn(href)) normalizeUrl(URL(URL(resultUrl), href).toString())
                            else null
                        }
                }
                // 🔹 Return first match or empty
                links.firstOrNull() ?: ""
            }
            is FetchResult.Error -> {
                println("Failed to fetch district page: $fetchResult")
                ""
            }
        }
    }


    /**
     * Checks whether exam results for a given exam type and year have been released.
     *
     * Note: This method does not distinguish between different failure causes.
     * Any error is treated as "results not released".
     */
    suspend fun isResultReleased(examType: ExamType, year: Int): Boolean {
        return try {
            getResultEntriesUrl(examType, year)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetches the exam results link for the given exam type and year.
     * Tries NECTA first for recent years, then Maktaba as fallback.
     * for primary schools it returns url for list of regions
     * for secondary schools it returns url for list of schools
     */
    suspend fun getResultEntriesUrl(examType: ExamType, year: Int): String {
        // Attempt NECTA first
        runCatching { fetchFromNecta(examType, year) }
            .getOrNull()?.let { if (isUrlValid(it)) return it }

        // Fallback to Maktaba
        runCatching { fetchFromMaktaba(examType, year) }
            .getOrNull()?.let { if (isUrlValid(it)) return it }

        throw IllegalStateException("No valid link found for ${examType.code} $year")
    }

    private suspend fun fetchFromNecta(examType: ExamType, year: Int): String {
        val url = "https://www.necta.go.tz/results/view/${examType.code.lowercase()}"
        val doc = fetchDocumentOrThrow(url, "NECTA")
        return doc.select("a[href]").firstOrNull {
            val lower = it.attr("href").lowercase()
            lower.contains("/$year/") && lower.contains(examType.code.lowercase())
        }?.attr("href") ?: throw Exception("NECTA link not found for ${examType.code} $year")
    }

    private suspend fun fetchFromMaktaba(examType: ExamType, year: Int): String {
        val url = "https://maktaba.tetea.org/results/"
        val doc = fetchDocumentOrThrow(url, "Maktaba")
        return doc.select("a[href]").firstOrNull {
            val href = it.attr("href").uppercase()
            when {
                examType == ExamType.FTNA && year == 2014 -> href.contains("FTSEE2014-2")
                else -> href.contains(examType.code, ignoreCase = true) && href.contains(year.toString())
            }
        }?.attr("href") ?: throw Exception("Maktaba link not found for ${examType.code} $year")
    }

    private suspend fun fetchDocumentOrThrow(url: String, sourceName: String) =
        when (val result = fetchDocument(url)) {
            is FetchResult.Success -> result.data
            is FetchResult.Error -> throw Exception("Failed to fetch $sourceName page: $result")
        }

    fun normalizeUrl(url: String): String {
        return url
            .replace("\\", "/")
            .replace(Regex("/+"), "/")
            .replace(":/", "://")
            // 🔥 Fix SFNA 2022 wrong "results/" insertion
            .replace(Regex("""/SFNA(\d{4})/results/""", RegexOption.IGNORE_CASE), "/SFNA$1/")
    }
    fun isUrlValid(url: String): Boolean {
        return try {
            val conn = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).execute()
            conn.statusCode() in 200..399
        } catch (e: Exception) {
            false
        }
    }
}



// Debug / test
fun main() = runBlocking {
    testIndividualSecondarySchoolUrl()
    //testIndividualPrimarySchool()
    //testDistricts()
    //testPrimarySchoolResults()
}


fun testIndividualPrimarySchool() = runBlocking {
    val testCases = listOf(
        Triple(ExamType.PSLE, 2013..2025, "0102073"),
        Triple(ExamType.SFNA, 2015..2025, "0102073")
    )

    println("===== GENERIC SCHOOL RESOLVER TEST =====")

    for ((exam, yearRange, schoolCode) in testCases) {
        for (year in yearRange) {
            try {
                val resultUrl = getPrimarySchoolUrl(exam, year, schoolCode)

                if (resultUrl.isNotEmpty()) {
                    println("✅ [$exam $year] -> $resultUrl")
                } else {
                    println("❌ [$exam $year] No result for $schoolCode")
                }
            } catch (e: Exception) {
                println("⚠️ [$exam $year] Error resolving $schoolCode: ${e.message}")
            }
        }
    }

    println("===== DONE =====")
}
fun testPrimarySchoolResults() = runBlocking {
    val examType = ExamType.SFNA
    val districtId = "0102" // Example: district ID
    val years = 2015..2025

    for (year in years) {
        try {
            val results = getPrimarySchoolsListUrl(examType, year, districtId)
            if (results.isNotEmpty()) {
                println("✅ $year -> $results")
            } else {
                println("❌ $year -> No results found for district $districtId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ $year -> Error fetching results")
        }
    }
}
fun testDistricts() = runBlocking {

    // Define exam-year ranges
    val testCases = listOf(
        ExamType.PSLE to (2013..2025),
        ExamType.SFNA to (2015..2025)
    )

    val regionCode = "01" // Example region code

    for ((examType, yearRange) in testCases) {
        for (year in yearRange) {
            try {
                // Step 1: get the region URL
                val regionUrl = getResultEntriesUrl(examType, year)

                // Step 2: get the district URL filtered by regionCode
                val districtUrl = extractDistrictsListUrl(examType, year, regionCode)

                if (districtUrl.isNotBlank()) {
                    println("✅ [$examType $year] District URL for region $regionCode: $districtUrl")
                } else {
                    println("❌ [$examType $year] No district found for region $regionCode")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ [$examType $year] Error occurred during testing")
            }
        }
    }
}
suspend fun testResultHomePage() {
    val years = 2014..2025
    val exams = listOf(
        //ExamType.ACSEE,
        //ExamType.CSEE,
        //ExamType.FTNA,
        ExamType.PSLE,
        //ExamType.SFNA
    )

    println("===== RESOLVER ENGINE TEST =====")
    for (exam in exams) {
        println("\n=== $exam ===")
        for (year in years) {
            try {
                val link = getResultEntriesUrl(exam, year)
                println("✅ $year -> $link")
            } catch (e: Exception) {
                println("❌ ${exam.code} $year -> ${e.message}")
            }
        }
    }
}
fun testIndividualSecondarySchoolUrl() = runBlocking {
    val testCases = listOf(
        Triple(ExamType.ACSEE, 2003..2025, "S0136"),
        Triple(ExamType.CSEE, 2003..2025, "S0147"),
        Triple(ExamType.FTNA, 2014..2025, "S0136"),
    )

    println("===== RANGE TEST =====")

    for ((exam, years, code) in testCases) {

        println("\n=== $exam (${years.first} - ${years.last}) ===")

        for (year in years) {
            try {
                val results = getSecondarySchoolUrl(exam, year, code)

                if (results.isNotEmpty()) {
                    println("✅ $year -> $results")
                } else {
                    println("❌ $year -> No results")
                }

            } catch (e: Exception) {
                println("❌ $year -> ${e.message}")
            }
        }
    }

    println("\n===== DONE =====")
}