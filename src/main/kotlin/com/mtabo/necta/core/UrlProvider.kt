package com.mtabo.necta.core

import com.mtabo.necta.models.ExamType
import com.mtabo.necta.parser.UrlParser

object UrlProvider {
    /**
     * todo
     * we will need to refractor this code so as
     * to call first the builder and then the parser
     * so we will need to check if the url is valid if not call the parser
     * and not rely on null
     */
    suspend fun getSchoolResultsUrl(
        exam: ExamType,
        year: Int,
        schoolCode: String
    ): String {
        return UrlBuilder.buildSchoolResultsUrl(exam, year, schoolCode)
            ?: UrlParser.extractSchoolResultsUrl(exam, year, schoolCode)
    }

    suspend fun getSchoolListUrl(
        exam: ExamType,
        year: Int,
        districtId: String? = null
    ): String {
        return UrlBuilder.buildSchoolListUrl(exam, year, districtId)
            ?: UrlParser.extractSchoolListUrl(exam, year, districtId)
    }

    suspend fun getDistrictListUrl(
        exam: ExamType,
        year: Int,
        regionCode: String
    ): String {
        return UrlBuilder.buildDistrictListUrl(exam, year, regionCode)
            ?: UrlParser.extractDistrictsListUrl(exam, year, regionCode)
    }

    suspend fun getRegionListUrl(
        exam: ExamType,
        year: Int
    ): String {
        return UrlBuilder.buildRegionListUrl(exam, year)
            ?: UrlParser.extractRegionsListUrl(exam, year)
    }
}