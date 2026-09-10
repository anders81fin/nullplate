package io.github.anders81fin.nullplate.domain

fun elapsedHours(sinceEpochSeconds: Long, nowEpochSeconds: Long): Double =
    ((nowEpochSeconds - sinceEpochSeconds) / 3600.0).coerceAtLeast(0.0)

fun progressFraction(elapsedHours: Double, targetHours: Double): Double =
    if (targetHours > 0) (elapsedHours / targetHours).coerceAtMost(1.0) else 0.0

// The Linux widget polled every second to spot a crossed hour boundary; with a
// known start instant every boundary is computable instead, so the scheduler
// asks for the next one rather than watching the clock.
fun nextHourBoundaryEpochSeconds(sinceEpochSeconds: Long, nowEpochSeconds: Long): Long {
    val elapsedFullHours = ((nowEpochSeconds - sinceEpochSeconds) / 3600).coerceAtLeast(0)
    return sinceEpochSeconds + (elapsedFullHours + 1) * 3600
}
