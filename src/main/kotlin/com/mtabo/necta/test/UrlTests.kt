package com.mtabo.necta.test

import com.mtabo.necta.models.Exam
import com.mtabo.necta.url.ParsedUrlResolver.extractDistrictListUrl
import com.mtabo.necta.url.ParsedUrlResolver.getPrimarySchoolUrl
import com.mtabo.necta.url.ParsedUrlResolver.getPrimarySchoolListUrl
import com.mtabo.necta.url.ParsedUrlResolver.getResultsEntryUrl
import com.mtabo.necta.url.ParsedUrlResolver.getSecondarySchoolUrl
import kotlinx.coroutines.runBlocking


// Debug / test
fun main() = runBlocking {
    testResultHomePage()
    //testIndividualSecondarySchoolUrl()
    //testIndividualPrimarySchool()
    //testDistricts()
    //testPrimarySchoolResults()
}


fun testIndividualPrimarySchool() = runBlocking {
    val testCases = listOf(
        Triple(Exam.PSLE, 2013..2025, "0102073"),
        Triple(Exam.SFNA, 2015..2025, "0102073")
    )

    println("===== GENERIC SCHOOL RESOLVER TEST =====")

    for ((exam, yearRange, schoolCode) in testCases) {
        for (year in yearRange) {
            try {
                val resultUrl = getPrimarySchoolUrl(exam, year, schoolCode)

                if (!resultUrl.isNullOrEmpty()) {
                    println("✅ [$exam $year] -> $resultUrl")
                } else {
                    println("❌ [$exam $year] No result for $schoolCode")
                }
            } catch (e: Exception) {
                println("⚠️ [$exam $year] Error resolving $schoolCode: ${e.message}")
            }
        }
    }

    println("===== DONE =====")
}
fun testPrimarySchoolResults() = runBlocking {
    val exam = Exam.SFNA
    val districtId = "0102" // Example: district ID
    val years = 2015..2025

    for (year in years) {
        try {
            val results = getPrimarySchoolListUrl(exam, year, districtId)
            if (!results.isNullOrEmpty()) {
                println("✅ $year -> $results")
            } else {
                println("❌ $year -> No results found for district $districtId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ $year -> Error fetching results")
        }
    }
}
fun testDistricts() = runBlocking {

    // Define exam-year ranges
    val testCases = listOf(
        Exam.PSLE to (2013..2025),
        Exam.SFNA to (2015..2025)
    )

    val regionCode = "01" // Example region code

    for ((examType, yearRange) in testCases) {
        for (year in yearRange) {
            try {
                // Step 1: get the region URL
                val regionUrl = getResultsEntryUrl(examType, year)

                // Step 2: get the district URL filtered by regionCode
                val districtUrl = extractDistrictListUrl(examType, year, regionCode)

                if (!districtUrl.isNullOrEmpty()) {
                    println("✅ [$examType $year] District URL for region $regionCode: $districtUrl")
                } else {
                    println("❌ [$examType $year] No district found for region $regionCode")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ [$examType $year] Error occurred during testing")
            }
        }
    }
}
suspend fun testResultHomePage() {
    val years = 2014..2026
    val exams = listOf(
        Exam.ACSEE,
        //Exam.CSEE,
        //Exam.FTNA,
        Exam.PSLE,
        //Exam.SFNA
    )

    println("===== RESOLVER ENGINE TEST =====")
    for (exam in exams) {
        println("\n=== $exam ===")
        for (year in years) {
            try {
                val link = getResultsEntryUrl(exam, year)
                println("✅ $year -> $link")
            } catch (e: Exception) {
                println("❌ ${exam.code} $year -> ${e.message}")
            }
        }
    }
}


fun testIndividualSecondarySchoolUrl() = runBlocking {
    val testCases = listOf(
        Triple(Exam.ACSEE, 2003..2026, "S0136"),
        Triple(Exam.CSEE, 2003..2026, "S0147"),
        Triple(Exam.FTNA, 2014..2026, "S0136"),
    )

    println("===== RANGE TEST =====")

    for ((exam, years, code) in testCases) {

        println("\n=== $exam (${years.first} - ${years.last}) ===")

        for (year in years) {
            try {
                val results = getSecondarySchoolUrl(exam, year, code)

                if (!results.isNullOrEmpty()) {
                    println("✅ $year -> $results")
                } else {
                    println("❌ $year -> No results")
                }

            } catch (e: Exception) {
                println("❌ $year -> ${e.message}")
            }
        }
    }

    println("\n===== DONE =====")
}