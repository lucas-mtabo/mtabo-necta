package com.mtabo.necta.url

import com.mtabo.necta.client.FetchResult
import com.mtabo.necta.client.JsoupClient.fetchDocument
import com.mtabo.necta.models.Exam
import com.mtabo.necta.url.ParsedUrlResolver.extractDistrictListUrl
import java.net.URL

object ParsedUrlResolver {

    suspend fun extractSchoolResultUrl(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): String? {
        return when(exam) {
            Exam.ACSEE, Exam.CSEE, Exam.FTNA ->
                getSecondarySchoolUrl(exam, year, schoolCode)

            Exam.PSLE, Exam.SFNA ->
                getPrimarySchoolUrl(exam, year, schoolCode)
        }
    }

    suspend fun extractSchoolListUrl(
        exam: Exam,
        year: Int,
        districtId: String? = null
    ): String? {
        return when(exam) {
            Exam.ACSEE, Exam.CSEE, Exam.FTNA ->
                getResultsEntryUrl(exam, year)

            Exam.PSLE, Exam.SFNA ->
                getPrimarySchoolListUrl(exam, year, districtId ?: "")
        }
    }

    suspend fun extractDistrictListUrl(
        exam: Exam,
        year: Int,
        regionCode: String
    ): String? {
        val regionsPageUrl = getResultsEntryUrl(exam, year)
        if (regionsPageUrl.isNullOrEmpty()) return null

        return when (val fetchResult = fetchDocument(regionsPageUrl)) {
            is FetchResult.Success -> {
                val doc = fetchResult.data

                // 🔹 Pattern differs for SFNA vs PSLE
                val regionPattern = when (exam) {
                    Exam.SFNA -> Regex("""(results/)?reg_ps${regionCode}\.htm""", RegexOption.IGNORE_CASE)
                    else -> Regex("""(results/)?reg_${regionCode}\.htm""", RegexOption.IGNORE_CASE)
                }

                val regionLink = doc.select("a[href]").firstOrNull { element ->
                    regionPattern.containsMatchIn(element.attr("href"))
                } ?: return null // region not found

                normalizeUrl(URL(URL(regionsPageUrl), regionLink.attr("href")).toString())
            }
            is FetchResult.Error -> {
               System.err.println("Failed to fetch regions page: $fetchResult")
                null
            }
        }
    }

    /**
     * Fetches the exam results link for the given exam type and year.
     * Tries NECTA first for recent years, then Maktaba tetea as fallback.
     * for primary schools it returns url for list of regions
     * for secondary schools it returns url for list of schools
     */
    suspend fun getResultsEntryUrl(exam: Exam, year: Int): String? {
        fetchFromNecta(exam, year)?.let { return it }

        fetchFromMaktaba(exam, year)?.let { return it }

        System.err.println("No result link found for ${exam.code} $year")
        return null
    }


    suspend fun getSecondarySchoolUrl(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): String? {
        val schoolListUrl = getResultsEntryUrl(exam, year)
            ?:return null

        return when (val fetchResult = fetchDocument(schoolListUrl)) {
            is FetchResult.Success -> {
                val doc = fetchResult.data

                // 🔹 Old broken CSEE pages
                if (exam == Exam.CSEE && year in 2003..2009) {
                    val regex = Regex(
                        """href\s*=\s*['"]([^'"]*${Regex.escape(schoolCode)}[^'"]*)['"]""",
                        RegexOption.IGNORE_CASE
                    )
                    regex.findAll(doc.outerHtml())
                        .map { match -> normalizeUrl(URL(URL(schoolListUrl), match.groupValues[1]).toString()) }
                        .firstOrNull()
                } else {
                    // 🔹 Modern pages
                    doc.select("a[href]").firstNotNullOfOrNull { element ->
                        val href = element.attr("href")
                        if (href.lowercase().contains(schoolCode.lowercase())) {
                            normalizeUrl(URL(URL(schoolListUrl), href).toString())
                        } else null
                    }
                }
            }

            is FetchResult.Error -> {
                System.err.println("Failed to fetch school list page: $fetchResult")
                null
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
     * @param exam The primary exam type (PSLE or SFNA).
     * @param year The year of the exam.
     * @param schoolCode The full school code (used to derive district and match the school link).
     * @return The URL of the school’s result page, or empty string if not found or an error occurs.
     * @throws IllegalArgumentException if the exam type is not PSLE or SFNA.
     */
    suspend fun getPrimarySchoolUrl(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): String? {
        // Step 1: Extract district code from the first 4 digits of schoolCode
        val districtId = schoolCode.take(4)

        // Step 2: Get district-level results page URL
        val districtUrl = getPrimarySchoolListUrl(exam, year, districtId)
        if (districtUrl.isNullOrEmpty()) return null

        // Step 3: Fetch district page HTML safely using fetchDocument
        return when (val schoolsInDistrict = fetchDocument(districtUrl)) {
            is FetchResult.Success -> {
                val doc = schoolsInDistrict.data

                // Step 4: Extract school-specific link
                val schoolLink = if (exam == Exam.PSLE && year in 2013..2015) {
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
                schoolLink
            }

            is FetchResult.Error -> {
                // Failed to fetch page; log error and return empty
                System.err.println("Failed to fetch district page: $schoolsInDistrict")
                null
            }
        }
    }


    /**
     * Fetches the URL of a specific district-level page for a primary exam.
     *
     * This function:
     * 1. Normalizes the district code and derives the region code.
     * 2. Fetches the region page URL via [extractDistrictListUrl].
     * 3. Parses the HTML using [fetchDocument] and extracts the district link.
     * 4. Handles old broken PSLE pages differently from modern pages.
     *
     * @param exam The primary exam type (PSLE or SFNA).
     * @param year The exam year.
     * @param districtCode The 4-digit district code.
     * @return The district result URL, or an empty string if not found or on error.
     */
    suspend fun getPrimarySchoolListUrl(
        exam: Exam,
        year: Int,
        districtCode: String
    ): String? {
        val regionCode = districtCode.dropLast(2)
        val districtUrl = extractDistrictListUrl(exam, year, regionCode)
        if (districtUrl.isNullOrEmpty()) return null

        return when (val fetchResult = fetchDocument(districtUrl)) {
            is FetchResult.Success -> {
                val doc = fetchResult.data
                val links: List<String> = if (exam == Exam.PSLE && year in 2013..2015) {
                    // 🔹 Old broken PSLE pages may have malformed HTML
                    val regex = Regex(
                        """href\s*=\s*['"]([^'"]*${Regex.escape(districtCode)}[^'"]*)['"]""",
                        RegexOption.IGNORE_CASE
                    )
                    regex.findAll(doc.outerHtml())
                        .map { match -> normalizeUrl(URL(URL(districtUrl), match.groupValues[1]).toString()) }
                        .toList()
                } else {
                    // 🔹 Modern pages
                    val districtPattern = when (exam) {
                        Exam.SFNA -> Regex("""distr_ps$districtCode.*\.htm""", RegexOption.IGNORE_CASE)
                        else -> Regex("""distr_$districtCode.*\.htm""", RegexOption.IGNORE_CASE)
                    }

                    doc.select("a[href]")
                        .mapNotNull { element ->
                            val href = element.attr("href")
                            if (districtPattern.containsMatchIn(href)) normalizeUrl(URL(URL(districtUrl), href).toString())
                            else null
                        }
                }
                // 🔹 Return first match or empty
                links.firstOrNull()
            }
            is FetchResult.Error -> {
                System.err.println("Failed to fetch district page: $fetchResult")
                null
            }
        }
    }


    private suspend fun fetchFromNecta(exam: Exam, year: Int): String? {
        val url = "https://www.necta.go.tz/results/view/${exam.code.lowercase()}"
        val doc = fetchDocumentOrThrow(url, "NECTA")

        return doc.select("a[href]").firstOrNull {
            val href = it.attr("href").lowercase()
            href.contains("/$year/") && href.contains(exam.code.lowercase())
        }?.attr("href")
    }

    private suspend fun fetchFromMaktaba(exam: Exam, year: Int): String? {
        val url = "https://maktaba.tetea.org/results/"
        val doc = fetchDocumentOrThrow(url, "Maktaba")

        return doc.select("a[href]").firstOrNull {
            val href = it.attr("href").uppercase()
            when {
                exam == Exam.FTNA && year == 2014 -> href.contains("FTSEE2014-2")
                else -> href.contains(exam.code, ignoreCase = true) && href.contains(year.toString())
            }
        }?.attr("href")
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
            .replace(Regex(
                """/SFNA(\d{4})/results/""",
                RegexOption.IGNORE_CASE),
                "/SFNA$1/"
            )
    }
}

