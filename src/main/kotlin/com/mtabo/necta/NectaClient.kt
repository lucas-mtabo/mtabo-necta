package com.mtabo.necta

import com.mtabo.necta.models.*
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
    ): NectaResult<List<Region>> {
        return repository.fetchRegions(exam, year)
    }

    // =========================================================
    // DISTRICTS
    // =========================================================

    suspend fun fetchDistricts(
        exam: Exam,
        year: Int,
        regionCode: String
    ): NectaResult<List<District>> {
        return repository.fetchDistricts(exam, year, regionCode)
    }

    // =========================================================
    // SCHOOLS
    // =========================================================

    suspend fun fetchSchools(
        exam: Exam,
        year: Int,
        districtCode: String? = null
    ): NectaResult<List<School>> {
        return repository.fetchSchools(exam, year, districtCode)
    }

    // =========================================================
    // RESULTS
    // =========================================================

    suspend fun fetchSchoolResults(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): NectaResult<List<StudentResult>> {
        return repository.fetchSchoolResult(exam, year, schoolCode)
    }

    // =========================================================
    // Performance
    // =========================================================

    suspend fun fetchSchoolPerformance(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): NectaResult<SchoolPerformance> {
        return repository.fetchSchoolPerformance(exam, year, schoolCode)
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