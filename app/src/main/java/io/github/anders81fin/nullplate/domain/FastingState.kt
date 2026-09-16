package io.github.anders81fin.nullplate.domain

import kotlinx.serialization.Serializable

@Serializable
data class FastingState(
    val fasting: Boolean = false,
    val startedAt: Long = 0,
    val targetHours: Double = DEFAULT_TARGET_HOURS,
    // Nothing is counting. Not derivable from the history: after the first fast
    // there is always a lastEnd to count an eating window from, so stopping the
    // clock needs a flag of its own. Defaulting to false is what lets a state
    // file written before this field existed keep its eating window running
    // instead of going quiet on upgrade.
    val idle: Boolean = false,
)
