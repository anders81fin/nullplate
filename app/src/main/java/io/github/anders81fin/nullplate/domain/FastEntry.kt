package io.github.anders81fin.nullplate.domain

import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class FastEntry(
    val start: Long,
    val end: Long,
    val targetHours: Double,
    val actualHours: Double,
)

fun completedEntry(state: FastingState, now: Long): FastEntry {
    val actual = (now - state.startedAt) / 3600.0
    return FastEntry(
        start = state.startedAt,
        end = now,
        targetHours = state.targetHours,
        actualHours = (actual * 100).roundToLong() / 100.0,
    )
}
