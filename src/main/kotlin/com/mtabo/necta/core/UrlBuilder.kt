package com.mtabo.necta.core

import com.mtabo.necta.models.ExamType

/**
 * Methods in this class
 * returns url that are already tested and working
 */
object UrlBuilder {
    fun buildSchoolResultsUrl(exams: ExamType, year: Int, schoolCode: String): String? {
        val normalizedId = schoolCode.lowercase()

        return when (exams) {
            ExamType.ACSEE -> when (year) {
                in 2005..2007 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/${normalizedId}.html"
                2008 -> null // No data for 2008
                in 2009..2024 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/${normalizedId}.htm"
                2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/acsee/results/$normalizedId.htm"
                else -> null
            }

            ExamType.CSEE -> when (year) {
                in 2003..2004, 2013 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/$normalizedId.html"
                2005 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2005/${normalizedId.uppercase()}.html"
                2006 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2006/${normalizedId.uppercase()}.htm"
                2007 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2007/${normalizedId.uppercase()}.HTM"
                in 2008..2011, in 2014..2023 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/$normalizedId.htm"
                2012 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2012-2/$normalizedId.htm"
                2024 ->
                    "https://onlinesys.necta.go.tz/results/$year/csee/results/$normalizedId.htm"
                2025 -> "https://matokeo.necta.go.tz/results/$year/csee/results/$normalizedId.htm"
                else -> null
            }

            ExamType.FTNA -> when(year) {
                2014 ->
                    "https://maktaba.tetea.org/exam-results/FTSEE2014-2/${normalizedId.uppercase()}.htm"  // includes zonal code 03_S0013-0.htm

                in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/FTNA$year/${normalizedId.uppercase()}.htm"

                2024 ->
                    "https://onlinesys.necta.go.tz/results/$year/ftna/results/${normalizedId.uppercase()}.htm"

                2025 -> "https://onlinesys.necta.go.tz/results/$year/ftna/results/${normalizedId}.htm"
                else -> null
            }

            ExamType.PSLE -> when(year) {
                in 2013..2023 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/shl_$normalizedId.htm"
                in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/results/shl_$normalizedId.htm"
                else -> null
            }

            ExamType.SFNA -> when(year) {
                in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/$normalizedId.htm"
                in 2024.. 2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/sfna/results/$normalizedId.htm"
                else -> null
            }
        }
    }

    fun buildSchoolListUrl(
        exams: ExamType,
        year: Int,
        districtId: String? = null
    ): String? {
        return when (exams) {
            ExamType.ACSEE -> when (year) {
                2008 -> null // No data for 2008
                in 2005..2009 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.html"
                in 2010..2014 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.htm"
                2015 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.html"
                in 2016..2024 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/index.htm"
                2025 -> "https://onlinesys.necta.go.tz/results/$year/acsee/index.htm"
                else -> null
            }

            ExamType.CSEE -> when (year) {
                in 2003..2004 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/olevel.html"
                2005 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/OLEVEL.html"
                in 2006..2011 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/olevel.htm"
                2012 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2012-2/olevel.htm"
                2013 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2013/olevel.html"
                2014 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2014/olevel2014.htm"
                2015 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2015/Olevel.htm"
                in 2016..2018 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/index.htm"
                in 2019..2021 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/csee.htm"
                in 2022..2023 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/index.htm"
                in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/csee/index.htm"
                else -> null
            }

            ExamType.FTNA -> when (year) {
                2014 ->
                    "https://maktaba.tetea.org/exam-results/FTSEE2014-2/formtwo-2014-2.htm"
                2015 ->
                    "https://maktaba.tetea.org/exam-results/FTNA2015/formtwo-2015.html"
                2016 ->
                    "https://maktaba.tetea.org/exam-results/FTNA2016/index.htm"
                in 2017..2023 ->
                    "https://maktaba.tetea.org/exam-results/FTNA$year/ftna.htm"
                in 2024 .. 2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/ftna/ftna.htm"
                else -> null
            }

            ExamType.PSLE -> when {
                year in 2013..2023 ->
                    if (districtId != null)
                        "https://maktaba.tetea.org/exam-results/PSLE$year/distr_${districtId}.htm"
                    else null
                year in 2024.. 2025 ->
                    if (districtId != null)
                        "https://onlinesys.necta.go.tz/results/$year/psle/results/distr_${districtId}.htm"
                    else null
                else -> null
            }

            ExamType.SFNA -> when {
                year in 2015..2023 ->
                    if (districtId != null)
                        "https://maktaba.tetea.org/exam-results/SFNA$year/distr_ps${districtId}.htm"
                    else null
                year in 2024.. 2025 ->
                    if (districtId != null)
                        "https://onlinesys.necta.go.tz/results/$year/sfna/results/distr_ps${districtId}.htm"
                    else null
                else -> null
            }
        }
    }

    fun buildRegionListUrl(exams: ExamType, year: Int): String? {
        return when (exams) {

            ExamType.SFNA -> when (year) {
                in 2015..2016 ->
                    "https://maktaba.tetea.org/exam-results/SFNA2015/index.htm"
                in 2017..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/sfna.htm"
                in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/sfna/sfna.htm"
                else -> null
            }

            ExamType.PSLE -> when (year) {
                2013 ->
                    "https://maktaba.tetea.org/exam-results/PSLE2013/psle.htm"
                2014 ->
                    "https://maktaba.tetea.org/exam-results/PSLE2014/psle2014.htm"
                in 2015..2022 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/psle.htm"
                in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/psle.htm"
                else -> null
            }

            else -> null
        }
    }

    fun buildDistrictListUrl(exams: ExamType, year: Int, regionCode: String): String? {
        return when (exams) {

            ExamType.PSLE -> when {
                year in 2013..2023 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/reg_${regionCode}.htm"
                year in 2024.. 2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/results/reg_${regionCode}.htm"
                else -> null
            }

            ExamType.SFNA -> when {
                year in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/reg_ps${regionCode}.htm"
                year in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/${year}/sfna/results/reg_ps${regionCode}.htm"
                else -> null
            }

            else -> null
        }
    }
}