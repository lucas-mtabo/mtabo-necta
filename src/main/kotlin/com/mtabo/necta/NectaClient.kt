package com.mtabo.necta

import com.mtabo.necta.models.District
import com.mtabo.necta.models.Exam
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School
import com.mtabo.necta.client.FetchResult
import com.mtabo.necta.url.ParsedUrlResolver.getResultsEntryUrl

object NectaClient {

    // Internal engine (hidden from users)
    private val repository = NectaRepository.create()

    // =========================================================
    // REGIONS
    // =========================================================

    suspend fun fetchRegions(
        exam: Exam,
        year: Int
    ): FetchResult<List<Region>> {
        return repository.fetchRegions(exam, year)
    }

    // =========================================================
    // DISTRICTS
    // =========================================================

    suspend fun fetchDistricts(
        exam: Exam,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {
        return repository.fetchDistricts(exam, year, regionCode)
    }

    // =========================================================
    // SCHOOLS
    // =========================================================

    suspend fun fetchSchools(
        exam: Exam,
        year: Int,
        districtCode: String? = null
    ): FetchResult<List<School>> {
        return repository.fetchSchools(exam, year, districtCode)
    }

    // =========================================================
    // RESULTS (STREAMING)
    // =========================================================

    suspend fun fetchSchoolResults(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): FetchResult<SchoolResultsStream> {
        return repository.fetchSchoolResult(exam, year, schoolCode)
    }


    /**
     * Checks whether exam results for a given exam type and year is available.
     *
     * Note: This method does not distinguish between different failure causes.
     * Any error is treated as "results not released".
     */
    suspend fun isResultAvailable(exam: Exam, year: Int): Boolean {
        return try {
            getResultsEntryUrl(exam, year)
            true
        } catch (e: Exception) {
            false
        }
    }
}