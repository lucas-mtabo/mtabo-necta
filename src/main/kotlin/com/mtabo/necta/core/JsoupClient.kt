package com.mtabo.necta.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.SocketTimeoutException

object JsoupClient {
    private const val DEFAULT_TIMEOUT = 10_000
    private const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun fetchDocument(
        url: String,
        retries: Int = 2,
        timeout: Int = DEFAULT_TIMEOUT,
        userAgent: String = DEFAULT_USER_AGENT
    ): FetchResult<Document> = withContext(Dispatchers.IO) {

        var lastError: Throwable? = null

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
                lastError = e
                if (attempt == retries) {
                    return@withContext FetchResult.Error.Timeout(e)
                }

            } catch (e: HttpStatusException) {
                return@withContext FetchResult.Error.Http(
                    statusCode = e.statusCode,
                    message = e.message
                )

            } catch (e: IOException) {
                lastError = e
                if (attempt == retries) {
                    return@withContext FetchResult.Error.Network(e)
                }

            } catch (e: Throwable) {
                return@withContext FetchResult.Error.Unknown(e)
            }

            delay(500L * (attempt + 1))
        }

        return@withContext FetchResult.Error.Unknown(
            lastError ?: RuntimeException("Unknown error")
        )
    }
}


sealed class FetchResult<out T> {

    data class Success<T>(val data: T) : FetchResult<T>()

    sealed class Error : FetchResult<Nothing>() {
        data class Network(val exception: IOException) : Error()
        data class Timeout(val exception: SocketTimeoutException) : Error()
        data class Http(val statusCode: Int, val message: String?) : Error()
        data class Unknown(val exception: Throwable) : Error()
    }
}