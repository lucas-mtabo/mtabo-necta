package com.mtabo.necta

sealed class NectaResult<out T> {

    data class Success<T>(
        val data: T
    ) : NectaResult<T>()

    data class NotFound(
        val message: String
    ) : NectaResult<Nothing>()

    data class Network(
        val message: String
    ) : NectaResult<Nothing>()

    data class Error(
        val message: String
    ) : NectaResult<Nothing>()
}