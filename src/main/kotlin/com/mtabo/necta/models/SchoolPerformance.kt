package org.example.com.mtabo.necta.models

import kotlinx.serialization.Serializable


@Serializable
data class SchoolPerformance(
    val performanceRows: List<PerformanceRow>
)


@Serializable
data class PerformanceRow(
    val values: List<String>
)