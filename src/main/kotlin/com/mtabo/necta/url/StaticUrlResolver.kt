package com.mtabo.necta.url

import com.mtabo.necta.models.Exam

/**
 * Methods in this class
 * returns url that are already tested and working
 */
object StaticUrlResolver {

    fun resolveSchoolResultUrl(
        exams: Exam,
        year: Int,
        schoolCode: String
    ): String {
        val normalizedId = schoolCode.lowercase()

        return when (exams) {
            Exam.ACSEE -> when (year) {
                in 2005..2007 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/${normalizedId}.html"
                2008 -> "" // No data for 2008
                in 2009..2024 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/${normalizedId}.htm"
                2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/acsee/results/$normalizedId.htm"
                else -> "https://matokeo.necta.go.tz/results/$year/acsee/results/$normalizedId.htm"
            }

            Exam.CSEE -> when (year) {
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
                else -> "https://matokeo.necta.go.tz/results/$year/csee/results/$normalizedId.htm"
            }

            Exam.FTNA -> when(year) {
                2014 ->
                    "https://maktaba.tetea.org/exam-results/FTSEE2014-2/${normalizedId.uppercase()}.htm"  // includes zonal code 03_S0013-0.htm

                in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/FTNA$year/${normalizedId.uppercase()}.htm"

                2024 ->
                    "https://onlinesys.necta.go.tz/results/$year/ftna/results/${normalizedId.uppercase()}.htm"

                else -> "https://onlinesys.necta.go.tz/results/$year/ftna/results/${normalizedId}.htm"
            }

            Exam.PSLE -> when(year) {
                in 2013..2023 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/shl_$normalizedId.htm"
                else ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/results/shl_$normalizedId.htm"
            }

            Exam.SFNA -> when(year) {
                in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/$normalizedId.htm"
                else ->
                    "https://onlinesys.necta.go.tz/results/$year/sfna/results/$normalizedId.htm"
            }
        }
    }

    fun resolveSchoolListUrl(
        exams: Exam,
        year: Int,
        districtId: String? = null
    ): String {
        return when (exams) {
            Exam.ACSEE -> when {
                year < 2005 -> ""
                year == 2008 -> "" // No data for 2008
                year in 2005..2009 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.html"
                year in 2010..2014 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.htm"
                year == 2015 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/alevel.html"
                year in 2016..2024 ->
                    "https://maktaba.tetea.org/exam-results/ACSEE$year/index.htm"
                else ->
                    "https://onlinesys.necta.go.tz/results/$year/acsee/index.htm"
            }

            Exam.CSEE -> when {
                year < 2003 -> ""
                year in 2003..2004 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/olevel.html"
                year == 2005 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/OLEVEL.html"
                year in 2006..2011 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/olevel.htm"
                year == 2012 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2012-2/olevel.htm"
                year == 2013 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2013/olevel.html"
                year == 2014 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2014/olevel2014.htm"
                year == 2015 ->
                    "https://maktaba.tetea.org/exam-results/CSEE2015/Olevel.htm"
                year in 2016..2018 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/index.htm"
                year in 2019..2021 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/csee.htm"
                year in 2022..2023 ->
                    "https://maktaba.tetea.org/exam-results/CSEE$year/index.htm"
                else ->
                    "https://onlinesys.necta.go.tz/results/$year/csee/index.htm"
            }

            Exam.FTNA -> when {
                year < 2014 -> ""
                year == 2014 ->
                    "https://maktaba.tetea.org/exam-results/FTSEE2014-2/formtwo-2014-2.htm"
                year == 2015 ->
                    "https://maktaba.tetea.org/exam-results/FTNA2015/formtwo-2015.html"
                year == 2016 ->
                    "https://maktaba.tetea.org/exam-results/FTNA2016/index.htm"
                year in 2017..2023 ->
                    "https://maktaba.tetea.org/exam-results/FTNA$year/ftna.htm"
                else ->
                    "https://onlinesys.necta.go.tz/results/$year/ftna/ftna.htm"
            }

            Exam.PSLE -> when {
                year < 2013 -> ""
                year in 2013..2023 ->
                    if (districtId != null)
                        "https://maktaba.tetea.org/exam-results/PSLE$year/distr_${districtId}.htm"
                    else ""
                else ->
                    if (districtId != null)
                        "https://onlinesys.necta.go.tz/results/$year/psle/results/distr_${districtId}.htm"
                    else ""
            }

            Exam.SFNA -> when {
                year < 2015 -> ""
                year in 2015..2023 ->
                    if (districtId != null)
                        "https://maktaba.tetea.org/exam-results/SFNA$year/distr_ps${districtId}.htm"
                    else ""
                else ->
                    if (districtId != null)
                        "https://onlinesys.necta.go.tz/results/$year/sfna/results/distr_ps${districtId}.htm"
                    else ""
            }
        }
    }

    fun resolveRegionListUrl(
        exams: Exam,
        year: Int
    ): String {
        return when (exams) {

            Exam.SFNA -> when {
                year < 2015 -> ""
                year in 2015..2016 ->
                    "https://maktaba.tetea.org/exam-results/SFNA2015/index.htm"
                year in 2017..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/sfna.htm"
                year in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/sfna/sfna.htm"
                else -> ""
            }

            Exam.PSLE -> when {
                year < 2013 -> ""
                year == 2013 ->
                    "https://maktaba.tetea.org/exam-results/PSLE2013/psle.htm"
                year == 2014 ->
                    "https://maktaba.tetea.org/exam-results/PSLE2014/psle2014.htm"
                year in 2015..2022 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/psle.htm"
                year in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/psle.htm"
                else -> ""
            }

            else -> ""
        }
    }

    fun resolveDistrictListUrl(
        exams: Exam,
        year: Int,
        regionCode: String
    ): String {
        return when (exams) {

            Exam.PSLE -> when {
                year < 2013 -> ""
                year in 2013..2023 ->
                    "https://maktaba.tetea.org/exam-results/PSLE$year/reg_${regionCode}.htm"
                year in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/psle/results/reg_${regionCode}.htm"
                else -> ""
            }

            Exam.SFNA -> when {
                year < 2015 -> ""
                year in 2015..2023 ->
                    "https://maktaba.tetea.org/exam-results/SFNA$year/reg_ps${regionCode}.htm"
                year in 2024..2025 ->
                    "https://onlinesys.necta.go.tz/results/$year/sfna/results/reg_ps${regionCode}.htm"
                else -> ""
            }

            else -> ""
        }
    }

//    fun resolveRegionListUrl(
//        exams: NectaExam,
//        year: Int
//    ): String? {
//        return when (exams) {
//
//            NectaExam.SFNA -> when (year) {
//                in 2015..2016 ->
//                    "https://maktaba.tetea.org/exam-results/SFNA2015/index.htm"
//                in 2017..2023 ->
//                    "https://maktaba.tetea.org/exam-results/SFNA$year/sfna.htm"
//                in 2024..2025 ->
//                    "https://onlinesys.necta.go.tz/results/$year/sfna/sfna.htm"
//                else -> null
//            }
//
//            NectaExam.PSLE -> when (year) {
//                2013 ->
//                    "https://maktaba.tetea.org/exam-results/PSLE2013/psle.htm"
//                2014 ->
//                    "https://maktaba.tetea.org/exam-results/PSLE2014/psle2014.htm"
//                in 2015..2022 ->
//                    "https://maktaba.tetea.org/exam-results/PSLE$year/psle.htm"
//                in 2024..2025 ->
//                    "https://onlinesys.necta.go.tz/results/$year/psle/psle.htm"
//                else -> null
//            }
//
//            else -> null
//        }
//    }
//
//    fun resolveDistrictListUrl(
//        exams: NectaExam,
//        year: Int,
//        regionCode: String
//    ): String? {
//        return when (exams) {
//
//            NectaExam.PSLE -> when (year) {
//                in 2013..2023 ->
//                    "https://maktaba.tetea.org/exam-results/PSLE$year/reg_${regionCode}.htm"
//
//                in 2024.. 2025 ->
//                    "https://onlinesys.necta.go.tz/results/$year/psle/results/reg_${regionCode}.htm"
//
//                else -> null
//            }
//
//            NectaExam.SFNA -> when (year) {
//                in 2015..2023 ->
//                    "https://maktaba.tetea.org/exam-results/SFNA$year/reg_ps${regionCode}.htm"
//
//                in 2024..2025 ->
//                    "https://onlinesys.necta.go.tz/results/${year}/sfna/results/reg_ps${regionCode}.htm"
//
//                else -> null
//            }
//
//            else -> null
//        }
//    }
}