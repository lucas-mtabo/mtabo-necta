package com.mtabo.necta

import com.mtabo.necta.client.FetchResult
import com.mtabo.necta.client.JsoupClient
import com.mtabo.necta.utils.TableUtils
import com.mtabo.necta.models.District
import com.mtabo.necta.models.Exam
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School
import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.parser.result.CseeAcseeParser
import com.mtabo.necta.parser.result.FtnaParser
import com.mtabo.necta.parser.PerformanceParser
import com.mtabo.necta.parser.result.PsleParser
import com.mtabo.necta.parser.parseSchools
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import com.mtabo.necta.parser.result.SfnaParser
import com.mtabo.necta.parser.parseDistricts
import com.mtabo.necta.parser.parseRegions
import com.mtabo.necta.url.UrlProvider
import com.mtabo.necta.models.SchoolPerformance
import org.jsoup.nodes.Document

class NectaRepository  (
    private val jsoupClient: JsoupClient,
    private val urlProvider: UrlProvider
) {

    // --- Regions ---
    suspend fun fetchRegions(
        exam: Exam,
        year: Int
    ): FetchResult<List<Region>> {

        val url = urlProvider.getRegionListUrl(exam, year)
            ?: return FetchResult.Error.Http(
                statusCode = 404,
                message = "Region list not found for $exam in $year"
            )

        return fetchAndParse(url, ::parseRegions)
    }

    // --- Districts ---
    suspend fun fetchDistricts(
        exam: Exam,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {

        val url = urlProvider.getDistrictListUrl(exam, year, regionCode)

        return fetchAndParse(url, ::parseDistricts)
    }

    // --- Schools ---
    suspend fun fetchSchools(
        exam: Exam,
        year: Int,
        districtCode: String?
    ): FetchResult<List<School>> {

        val url = districtCode?.let {
            urlProvider.getSchoolListUrl(exam, year, it)
        }

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
        exam: Exam,
        year: Int,
        schoolCode: String
    ): FetchResult<> {

        val url = urlProvider.getSchoolResultsUrl(exam, year, schoolCode)

        return when (val result = jsoupClient.fetchDocument(url)) {

            is FetchResult.Success -> {
                val doc = result.data

                val performance = PerformanceParser
                    .parsePerformance(doc, exam, year)

                val students: List<StudentResult> = when (exam) {

                    Exam.ACSEE, Exam.CSEE ->
                        CseeAcseeParser.parseResults(
                            TableUtils.fetchCseeResultTable(doc)
                        )

                    Exam.FTNA ->
                        FtnaParser.parseResults(doc, year)

                    Exam.PSLE ->
                        PsleParser.parseResults(
                            TableUtils.fetchPsleResultTable(doc), year
                        )

                    Exam.SFNA ->
                        SfnaParser.parseResults(doc, year)
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