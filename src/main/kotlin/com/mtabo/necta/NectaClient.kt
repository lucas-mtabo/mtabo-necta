package com.mtabo.necta

import com.mtabo.necta.core.NectaRepository
import com.mtabo.necta.core.SchoolResultsStream
import com.mtabo.necta.models.District
import com.mtabo.necta.models.NectaExam
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School
import com.mtabo.necta.core.FetchResult
import com.mtabo.necta.url.ParsedUrlResolver.getResultsEntryUrl

object NectaClient {

    // Internal engine (hidden from users)
    private val repository = NectaRepository.create()

    // =========================================================
    // REGIONS
    // =========================================================

    suspend fun fetchRegions(
        nectaExam: NectaExam,
        year: Int
    ): FetchResult<List<Region>> {
        return repository.fetchRegions(nectaExam, year)
    }

    // =========================================================
    // DISTRICTS
    // =========================================================

    suspend fun fetchDistricts(
        nectaExam: NectaExam,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {
        return repository.fetchDistricts(nectaExam, year, regionCode)
    }

    // =========================================================
    // SCHOOLS
    // =========================================================

    suspend fun fetchSchools(
        nectaExam: NectaExam,
        year: Int,
        districtCode: String? = null
    ): FetchResult<List<School>> {
        return repository.fetchSchools(nectaExam, year, districtCode)
    }

    // =========================================================
    // RESULTS (STREAMING)
    // =========================================================

    suspend fun fetchSchoolResults(
        nectaExam: NectaExam,
        year: Int,
        schoolCode: String
    ): FetchResult<SchoolResultsStream> {
        return repository.fetchSchoolResult(nectaExam, year, schoolCode)
    }


    /**
     * Checks whether exam results for a given exam type and year is available.
     *
     * Note: This method does not distinguish between different failure causes.
     * Any error is treated as "results not released".
     */
    suspend fun isResultAvailable(exam: NectaExam, year: Int): Boolean {
        return try {
            getResultsEntryUrl(exam, year)
            true
        } catch (e: Exception) {
            false
        }
    }
}