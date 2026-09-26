package com.mtabo.necta.url

import com.mtabo.necta.client.JsoupClient
import com.mtabo.necta.models.Exam


/**
 * Resolves the URL.
 *
 * Tries the static URL first. If the URL is not reachable,
 * falls back to the parsed URL resolver.
 */
object UrlProvider {

    suspend fun getSchoolResultsUrl(
        exam: Exam,
        year: Int,
        schoolCode: String
    ): String? {
        val staticUrl = StaticUrlResolver.resolveSchoolResultUrl(
            exam,
            year,
            schoolCode
        )

        if (staticUrl.isNotEmpty() && JsoupClient.isUrlReachable(staticUrl)) {
            return staticUrl
        }

        return ParsedUrlResolver.extractSchoolResultUrl(
            exam,
            year,
            schoolCode
        )
    }

    suspend fun getSchoolListUrl(
        exam: Exam,
        year: Int,
        districtId: String? = null
    ): String? {
        val staticUrl = StaticUrlResolver.resolveSchoolListUrl(
            exam,
            year,
            districtId
        )

        if (staticUrl.isNotEmpty() && JsoupClient.isUrlReachable(staticUrl)) {
            return staticUrl
        }

        return ParsedUrlResolver.extractSchoolListUrl(
            exam,
            year,
            districtId
        )
    }

    suspend fun getDistrictListUrl(
        exam: Exam,
        year: Int,
        regionCode: String
    ): String? {
        val staticUrl = StaticUrlResolver.resolveDistrictListUrl(
            exam,
            year,
            regionCode
        )

        if (staticUrl.isNotEmpty() && JsoupClient.isUrlReachable(staticUrl)) {
            return staticUrl
        }

        return ParsedUrlResolver.extractDistrictListUrl(
            exam,
            year,
            regionCode
        )
    }

    suspend fun getRegionListUrl(
        exam: Exam,
        year: Int
    ): String? {
        val staticUrl = StaticUrlResolver.resolveRegionListUrl(
            exam,
            year
        )

        if (staticUrl.isNotEmpty() && JsoupClient.isUrlReachable(staticUrl)) {
            return staticUrl
        }

        return ParsedUrlResolver.getResultsEntryUrl(
            exam,
            year
        )
    }
}