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
    abstract val subjectDetails: List<SubjectDetail>

    @Serializable
    @SerialName("ftna")
    data class FtnaStudentResult(
        val premNo: String?,
        val points: String,
        val division: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjectDetails: List<SubjectDetail>
    ) : StudentResult()

    @Serializable
    @SerialName("secondary")
    data class CseeOrAcseeResult(
        val points: String,
        val division: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjectDetails: List<SubjectDetail>
    ) : StudentResult()

    @Serializable
    @SerialName("primary")
    data class PsleAndSfnaResult(
        val premNo: String?,
        val grade: String,
        override val indexNo: String,
        override val name: String?,
        override val sex: String,
        override val subjectDetails: List<SubjectDetail>
    ) : StudentResult()
}



