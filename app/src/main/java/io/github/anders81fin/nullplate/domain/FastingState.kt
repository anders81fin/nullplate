package io.github.anders81fin.nullplate.domain

import kotlinx.serialization.Serializable

@Serializable
data class FastingState(
    val fasting: Boolean = false,
    val startedAt: Long = 0,
    val targetHours: Double = DEFAULT_TARGET_HOURS,
)
