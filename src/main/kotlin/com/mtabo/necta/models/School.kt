package com.mtabo.necta.models

import kotlinx.serialization.Serializable

@Serializable
data class School(
    val code: String,
    val name: String
)

@Serializable
data class PerformanceRow(
    val values: List<String>
)

@Serializable
data class SchoolPerformance(
    val performanceRows: List<PerformanceRow>
)

@Serializable
data class SchoolResult(
    val performance: SchoolPerformance,
    val students: List<StudentResult>
)
