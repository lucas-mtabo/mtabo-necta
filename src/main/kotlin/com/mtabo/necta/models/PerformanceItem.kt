package com.mtabo.necta.models

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceItem(
    val values: List<String>
)