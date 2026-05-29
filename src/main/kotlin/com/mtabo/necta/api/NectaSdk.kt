package org.example.com.mtabo.necta.api

import com.mtabo.necta.core.FetchResult
import com.mtabo.necta.core.NectaRepository
import com.mtabo.necta.core.SchoolResultsStream
import com.mtabo.necta.models.District
import com.mtabo.necta.models.ExamType
import com.mtabo.necta.models.Region
import com.mtabo.necta.models.School

object NectaSdk {

    // Internal engine (hidden from users)
    private val repository = NectaRepository.create()

    // =========================================================
    // REGIONS
    // =========================================================

    suspend fun fetchRegions(
        examType: ExamType,
        year: Int
    ): FetchResult<List<Region>> {
        return repository.fetchRegions(examType, year)
    }

    // =========================================================
    // DISTRICTS
    // =========================================================

    suspend fun fetchDistricts(
        examType: ExamType,
        year: Int,
        regionCode: String
    ): FetchResult<List<District>> {
        return repository.fetchDistricts(examType, year, regionCode)
    }

    // =========================================================
    // SCHOOLS
    // =========================================================

    suspend fun fetchSchools(
        examType: ExamType,
        year: Int,
        districtCode: String? = null
    ): FetchResult<List<School>> {
        return repository.fetchSchools(examType, year, districtCode)
    }

    // =========================================================
    // RESULTS (STREAMING)
    // =========================================================

    suspend fun fetchSchoolResults(
        examType: ExamType,
        year: Int,
        schoolCode: String
    ): FetchResult<SchoolResultsStream> {
        return repository.fetchSchoolResult(examType, year, schoolCode)
    }
}