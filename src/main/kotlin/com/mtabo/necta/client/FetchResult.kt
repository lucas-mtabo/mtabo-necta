package com.mtabo.necta.client

import java.io.IOException
import java.net.SocketTimeoutException

sealed class FetchResult<out T> {

    data class Success<T>(val data: T) : FetchResult<T>()

    sealed class Error : FetchResult<Nothing>() {
        data class Network(val exception: IOException) : Error()
        data class Timeout(val exception: SocketTimeoutException) : Error()
        data class Http(val statusCode: Int, val message: String?) : Error()
        data class Unknown(val exception: Throwable) : Error()
    }
}