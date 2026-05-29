package com.mtabo.necta.core

import com.mtabo.necta.utils.TableUtils
import com.mtabo.necta.models.District
import com.mtabo.necta.models.ExamType
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School
import com.mtabo.necta.models.SchoolPerformance
import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.parser.CseeAcseeParser
import com.mtabo.necta.parser.FtnaParser
import com.mtabo.necta.parser.PerformanceParser
import com.mtabo.necta.parser.PsleParser
import com.mtabo.necta.parser.parseSchools
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import com.mtabo.necta.parser.SfnaParser
import com.mtabo.necta.parser.parseDistricts
import com.mtabo.necta.parser.parseRegions
import org.jsoup.nodes.Document

class NectaRepository  (
    private val jsoupClient: JsoupClient,
    private val urlProvider: UrlProvider
) {

    // --- Regions ---
    suspend fun fetchRegions(
        exams: ExamType,
        year: Int
    ): FetchResult<List<Region>> {

        val url = urlProvider.getRegionListUrl(exams, year)

        return fetchAndParseWithRetry(url, ::parseRegions)
    }

    // --- Districts ---
    suspend fun fetchDistricts(
        exams: ExamType,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {

        val url = urlProvider.getDistrictListUrl(exams, year, regionCode)

        return fetchAndParseWithRetry(url, ::parseDistricts)
    }

    // --- Schools ---
    suspend fun fetchSchools(
        exams: ExamType,
        year: Int,
        districtCode: String?
    ): FetchResult<List<School>> {

        val url = districtCode?.let {
            urlProvider.getSchoolListUrl(exams, year, it)
        } ?: urlProvider.getSchoolListUrl(exams, year)

        var lastError: FetchResult.Error? = null
        val retries = 2

        repeat(retries + 1) { attempt ->

            when (val result = jsoupClient.fetchDocument(url)) {

                is FetchResult.Success -> {
                    val schools = parseSchools(result.data, exams)
                    return FetchResult.Success(schools)
                }

                is FetchResult.Error.Timeout,
                is FetchResult.Error.Network -> {
                    lastError = result
                    if (attempt < retries) delay(500L * (attempt + 1))
                }

                is FetchResult.Error.Http,
                is FetchResult.Error.Unknown -> {
                    return result
                }
            }
        }

        return lastError ?: FetchResult.Error.Unknown(
            RuntimeException("Failed to fetch schools after retries")
        )
    }

    // --- School Results (Flow streaming) ---
    suspend fun fetchSchoolResult(
        examType: ExamType,
        year: Int,
        schoolCode: String
    ): FetchResult<SchoolResultsStream> {

        val url = urlProvider.getSchoolResultsUrl(examType, year, schoolCode)

        return when (val result = jsoupClient.fetchDocument(url)) {

            is FetchResult.Success -> {
                val doc = result.data

                val performance = PerformanceParser
                    .parsePerformance(doc, examType, year)

                val students: Flow<StudentResult> = when (examType) {

                    ExamType.ACSEE, ExamType.CSEE ->
                        CseeAcseeParser.parseResults(
                            TableUtils.fetchCseeResultTable(doc)
                        )

                    ExamType.FTNA ->
                        FtnaParser.parseResults(doc, year)

                    ExamType.PSLE ->
                        PsleParser.parseResults(
                            TableUtils.fetchPsleResultTable(doc), year
                        )

                    ExamType.SFNA ->
                        SfnaParser.parseResultsFlow(doc, year)
                }

                FetchResult.Success(
                    SchoolResultsStream(
                        performance = performance,
                        students = students
                    )
                )
            }

            is FetchResult.Error -> result
        }
    }

    // --- Helper for consistent error creation ---
    private fun error(message: String): FetchResult.Error.Unknown {
        return FetchResult.Error.Unknown(IllegalArgumentException(message))
    }

    // --- Generic helper with retry (FIXED) ---
    private suspend fun <T> fetchAndParseWithRetry(
        url: String,
        parser: suspend (Document) -> T,
        retries: Int = 2
    ): FetchResult<T> {

        var lastError: FetchResult.Error? = null

        repeat(retries + 1) { attempt ->
            when (val result = jsoupClient.fetchDocument(url)) {

                is FetchResult.Success -> {
                    return FetchResult.Success(parser(result.data))
                }

                is FetchResult.Error.Timeout,
                is FetchResult.Error.Network -> {
                    lastError = result
                    if (attempt < retries) delay(500L * (attempt + 1))
                }

                is FetchResult.Error.Http,
                is FetchResult.Error.Unknown -> {
                    return result
                }
            }
        }

        return lastError ?: FetchResult.Error.Unknown(
            RuntimeException("Failed after retries: $url")
        )
    }

    companion object {
        fun create(): NectaRepository {
            return NectaRepository(
                jsoupClient = JsoupClient,
                urlProvider = UrlProvider
            )
        }
    }
}

// --- Domain wrapper ---
data class SchoolResultsStream(
    val performance: SchoolPerformance,
    val students: Flow<StudentResult>
)