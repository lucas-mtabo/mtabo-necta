package com.mtabo.necta.models

import kotlinx.serialization.Serializable

/**
 * Represents a single subject entry within any exam result.
 */
@Serializable
data class Subject(
    val name: String,
    val grade: String
)