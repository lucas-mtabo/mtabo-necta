package com.mtabo.necta.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed class hierarchy for all exam result types.
 * Covers FTNA, CSEE, ACSEE, PSLE, etc.
 */
@Serializable
sealed class StudentResult {

    abstract val indexNo: String
    abstract val name: String?
    abstract val sex: String
    abstract val subjects: List<Subject>

    @Serializable
    @SerialName("ftna")
    data class FtnaResult(
        val premNo: String?,
        val points: String,
        val division: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjects: List<Subject>
    ) : StudentResult()

    @Serializable
    @SerialName("secondary")
    data class SecondaryStudentResult(
        val points: String,
        val division: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjects: List<Subject>
    ) : StudentResult()

    @Serializable
    @SerialName("primary")
    data class PrimaryStudentResult(
        val premNo: String?,
        val grade: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjects: List<Subject>
    ) : StudentResult()
}



