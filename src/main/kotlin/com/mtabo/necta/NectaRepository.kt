package com.mtabo.necta

import com.mtabo.necta.client.FetchResult
import com.mtabo.necta.client.JsoupClient
import com.mtabo.necta.models.*
import com.mtabo.necta.parser.PerformanceParser
import com.mtabo.necta.parser.parseDistricts
import com.mtabo.necta.parser.parseRegions
import com.mtabo.necta.parser.parseSchools
import com.mtabo.necta.parser.result.CseeAcseeParser
import com.mtabo.necta.parser.result.FtnaParser
import com.mtabo.necta.parser.result.PsleParser
import com.mtabo.necta.parser.result.SfnaParser
import com.mtabo.necta.url.UrlProvider
import org.jsoup.nodes.Document

class NectaRepository(
    private val jsoupClient: JsoupClient,
    private val urlProvider: UrlProvider
) {

    // --- Regions ---
    suspend fun fetchRegions(
        exam: Exam,
        year: Int
    ): NectaResult<List<Region>> =
        fetch(
            url = urlProvider.getRegionListUrl(exam, year),
            parser = ::parseRegions
        )

    // --- Districts ---
    suspend fun fetchDistricts(
        exam: Exam,
        year: Int,
        regionCode: String
    ): NectaResult<List<District>> =
        fetch(
            url = urlProvider.getDistrictListUrl(
                exam,
                year,
                regionCode
            ),
            parser = ::parseDistricts
        )

    // --- Schools ---
    suspend fun fetchSchools(
        exam: Exam,
        year: Int,
        districtCode: String?
    ): NectaResult<List<School>> =
        fetch(
            url = urlProvider.getSchoolListUrl(
                exam,
                year,
                districtCode
            )
        ) { doc ->
            parseSchools(doc, exam)
        }

    // --- School Results ---
    suspend fun fetchSchoolResult(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): NectaResult<List<StudentResult>> =
        fetch(
            url = urlProvider.getSchoolResultsUrl(
                exam,
                year,
                schoolCode
            )
        ) { doc ->
            parseStudentResults(
                doc = doc,
                exam = exam,
                year = year
            )
        }

    // --- School Performance ---
    suspend fun fetchSchoolPerformance(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): NectaResult<SchoolPerformance> =
        fetch(
            url = urlProvider.getSchoolResultsUrl(
                exam,
                year,
                schoolCode
            )
        ) { doc ->
            PerformanceParser.parsePerformance(
                doc,
                exam,
                year
            )
        }

    /**
     * Common fetch pipeline:
     *
     * 1. Validate URL
     * 2. Fetch document
     * 3. Parse document
     * 4. Convert FetchResult to NectaResult
     */
    private suspend fun <T> fetch(
        url: String?,
        parser: suspend (Document) -> T
    ): NectaResult<T> {

        val resolvedUrl = url
            ?: return NectaResult.NotFound(
                "The URL is not available or may be unreachable"
            )

        return when (val result = jsoupClient.fetchDocument(resolvedUrl)) {

            is FetchResult.Success ->
                NectaResult.Success(
                    parser(result.data)
                )

            is FetchResult.Error.Network ->
                NectaResult.Network(
                    "Unable to connect to NECTA"
                )

            is FetchResult.Error.Timeout ->
                NectaResult.Network(
                    "Connection to NECTA timed out"
                )

            is FetchResult.Error.Http ->
                NectaResult.Error(
                    result.message
                        ?: "NECTA returned HTTP ${result.statusCode}"
                )

            is FetchResult.Error.Unknown ->
                NectaResult.Error(
                    result.exception.message
                        ?: "An unexpected error occurred"
                )
        }
    }

    /**
     * Selects the appropriate parser for each examination.
     */
    private fun parseStudentResults(
        doc: Document,
        exam: Exam,
        year: Int
    ): List<StudentResult> =
        when (exam) {

            Exam.ACSEE,
            Exam.CSEE ->
                CseeAcseeParser.parseResults(doc)

            Exam.FTNA ->
                FtnaParser.parseResults(doc, year)

            Exam.PSLE ->
                PsleParser.parseResults(doc, year)

            Exam.SFNA ->
                SfnaParser.parseResults(doc, year)
        }

    companion object {
        fun create(): NectaRepository =
            NectaRepository(
                jsoupClient = JsoupClient,
                urlProvider = UrlProvider
            )
    }
}