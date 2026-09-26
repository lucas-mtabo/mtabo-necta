package com.mtabo.necta.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.milliseconds

object JsoupClient {

    private const val DEFAULT_TIMEOUT = 10_000
    private const val DEFAULT_RETRIES = 2

    private const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Safari/537.36"

    suspend fun fetchDocument(
        url: String,
        retries: Int = DEFAULT_RETRIES,
        timeout: Int = DEFAULT_TIMEOUT,
        userAgent: String = DEFAULT_USER_AGENT
    ): FetchResult<Document> = withContext(Dispatchers.IO) {

        repeat(retries + 1) { attempt ->

            try {
                val document = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeout)
                    .header("Accept", "text/html")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .get()

                return@withContext FetchResult.Success(document)

            } catch (e: SocketTimeoutException) {

                if (attempt == retries) {
                    return@withContext FetchResult.Error.Timeout(e)
                }

            } catch (e: HttpStatusException) {

                return@withContext FetchResult.Error.Http(
                    statusCode = e.statusCode,
                    message = e.message
                )

            } catch (e: IOException) {

                if (attempt == retries) {
                    return@withContext FetchResult.Error.Network(e)
                }

            } catch (e: Exception) {

                return@withContext FetchResult.Error.Unknown(e)
            }

            delay((500L * (1L shl attempt)).milliseconds)
        }

        error("Unreachable")
    }

    suspend fun isUrlReachable(
        url: String,
        timeout: Int = DEFAULT_TIMEOUT,
        userAgent: String = DEFAULT_USER_AGENT
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            val response = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .method(Connection.Method.HEAD)
                .execute()

            response.statusCode() in 200..399

        } catch (e: Exception) {
            false
        }
    }
}
