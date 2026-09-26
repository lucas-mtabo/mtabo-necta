package com.mtabo.necta.test

import com.mtabo.necta.client.FetchResult
import com.mtabo.necta.client.JsoupClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    //testIsUrlReachable()

    testJsoupFetch()

}

suspend fun testJsoupFetch(){
    println("========== Jsoup Fetching ==========")

    val validUrl = "https://www.necta.go.tz/"
    when (val result = JsoupClient.fetchDocument(validUrl)) {

        is FetchResult.Success -> {
            println("Fetch successful!")
            println("Title: ${result.data.title()}")
            println("URL: ${result.data.baseUri()}")
        }

        is FetchResult.Error.Network -> {
            println("Network error:")
            println(result.exception.message)
        }

        is FetchResult.Error.Timeout -> {
            println("Timeout:")
            println(result.exception.message)
        }

        is FetchResult.Error.Http -> {
            println("HTTP error:")
            println("Status: ${result.statusCode}")
            println("Message: ${result.message}")
        }

        is FetchResult.Error.Unknown -> {
            println("Unknown error:")
            println(result.exception.message)
        }
    }
}

suspend fun testIsUrlReachable() {

    val validUrl =
        "https://www.necta.go.tz/"

    val isInvalidUrl =
        "htt://www.necta.go.tz"

    println("========== URL REACHABILITY ==========")

    val validReachable = JsoupClient.isUrlReachable(validUrl)

    println("Valid URL:")
    println("  $validUrl")
    println("  Reachable: $validReachable")

    val isInvalidReachable = JsoupClient.isUrlReachable(isInvalidUrl)

    println("\nInvalid URL:")
    println("  $isInvalidUrl")
    println("  Reachable: $isInvalidReachable")
}