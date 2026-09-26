package com.mtabo.necta.test

import com.mtabo.necta.NectaRepository
import com.mtabo.necta.NectaResult
import com.mtabo.necta.models.Exam
import kotlinx.coroutines.runBlocking

fun main() {

    val repository = NectaRepository.create()

    // testFetchRegions(repository)

    // Uncomment one at a time when testing:

    // testFetchDistricts(repository)

    // testFetchSchools(repository)

    // testFetchSchoolResults(repository)

     testFetchSchoolPerformance(repository)
}



fun testFetchRegions(repository: NectaRepository) = runBlocking {
    println("\n========== TEST: FETCH REGIONS  FOR PSLE AND SFNA ONLY==========")

    val result = repository.fetchRegions(
        exam = Exam.PSLE,
        year = 2023
    )

    printlnResult(result)
}

fun testFetchDistricts(repository: NectaRepository) = runBlocking {
    println("\n========== TEST: FETCH DISTRICTS for PSLE and SFNA only==========")

    val result = repository.fetchDistricts(
        exam = Exam.PSLE,
        year = 2023,
        regionCode = "01"
    )

    printlnResult(result)
}

fun testFetchSchools(repository: NectaRepository) = runBlocking {
    println("\n========== TEST: FETCH SCHOOLS ==========")

    val result = repository.fetchSchools(
        exam = Exam.CSEE,
        year = 2023,
        districtCode = "0101"
    )

    printlnResult(result)
}

fun testFetchSchoolResults(repository: NectaRepository) = runBlocking {
    println("\n========== TEST: FETCH SCHOOL RESULTS ==========")

    val result = repository.fetchSchoolResult(
        exam = Exam.CSEE,
        year = 2025,
        schoolCode = "S0136"
    )

    printlnResult(result)
}

fun testFetchSchoolPerformance(repository: NectaRepository) = runBlocking {
    println("\n========== TEST: FETCH SCHOOL PERFORMANCE ==========")

    val result = repository.fetchSchoolPerformance(
        exam = Exam.CSEE,
        year = 2023,
        schoolCode = "S0136"
    )

    printlnResult(result)
}


fun <T> printlnResult(result: NectaResult<T>) {

    when (result) {

        is NectaResult.Success -> {
            println("SUCCESS")
            println("Data: ${result.data}")
        }

        is NectaResult.NotFound -> {
            println("NOT FOUND")
            println("Message: ${result.message}")
        }

        is NectaResult.Network -> {
            println("NETWORK ERROR")
            println("Message: ${result.message}")
        }

        is NectaResult.Error -> {
            println("ERROR")
            println("Message: ${result.message}")
        }
    }
}