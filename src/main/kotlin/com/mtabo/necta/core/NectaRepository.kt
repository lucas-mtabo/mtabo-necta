package com.mtabo.necta.core

import com.mtabo.necta.utils.TableUtils
import com.mtabo.necta.models.District
import com.mtabo.necta.models.NectaExam
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School
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
import com.mtabo.necta.url.UrlProvider
import org.example.com.mtabo.necta.models.SchoolPerformance
import org.jsoup.nodes.Document

class NectaRepository  (
    private val jsoupClient: JsoupClient,
    private val urlProvider: UrlProvider
) {

    // --- Regions ---
    suspend fun fetchRegions(
        exam: NectaExam,
        year: Int
    ): FetchResult<List<Region>> {

        val url = urlProvider.getRegionListUrl(exam, year)

        return fetchAndParse(url, ::parseRegions)
    }

    // --- Districts ---
    suspend fun fetchDistricts(
        exam: NectaExam,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {

        val url = urlProvider.getDistrictListUrl(exam, year, regionCode)

        return fetchAndParse(url, ::parseDistricts)
    }

    // --- Schools ---
    suspend fun fetchSchools(
        exam: NectaExam,
        year: Int,
        districtCode: String?
    ): FetchResult<List<School>> {

        val url = districtCode?.let {
            urlProvider.getSchoolListUrl(exam, year, it)
        } ?: urlProvider.getSchoolListUrl(exam, year)

        var lastError: FetchResult.Error? = null
        val retries = 2

        repeat(retries + 1) { attempt ->

            when (val result = jsoupClient.fetchDocument(url)) {

                is FetchResult.Success -> {
                    val schools = parseSchools(result.data, exam)
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
        exam: NectaExam,
        year: Int,
        schoolCode: String
    ): FetchResult<SchoolResultsStream> {

        val url = urlProvider.getSchoolResultsUrl(exam, year, schoolCode)

        return when (val result = jsoupClient.fetchDocument(url)) {

            is FetchResult.Success -> {
                val doc = result.data

                val performance = PerformanceParser
                    .parsePerformance(doc, exam, year)

                val students: Flow<StudentResult> = when (exam) {

                    NectaExam.ACSEE, NectaExam.CSEE ->
                        CseeAcseeParser.parseResults(
                            TableUtils.fetchCseeResultTable(doc)
                        )

                    NectaExam.FTNA ->
                        FtnaParser.parseResults(doc, year)

                    NectaExam.PSLE ->
                        PsleParser.parseResults(
                            TableUtils.fetchPsleResultTable(doc), year
                        )

                    NectaExam.SFNA ->
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



    private suspend fun <T> fetchAndParse(
        url: String,
        parser: suspend (Document) -> T
    ): FetchResult<T> =
        when (val result = jsoupClient.fetchDocument(url)) {
            is FetchResult.Success -> FetchResult.Success(parser(result.data))
            is FetchResult.Error -> result
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