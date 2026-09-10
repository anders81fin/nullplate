package io.github.anders81fin.nullplate.domain

const val RECENT_COUNT = 3
const val RECENT_MIN_HOURS = 12.0
const val LONGEST_COUNT = 3

fun computeStreak(entries: List<FastEntry>): Int {
    var streak = 0
    for (entry in entries.asReversed()) {
        if (entry.actualHours < entry.targetHours) break
        streak++
    }
    return streak
}

fun recentLongFasts(entries: List<FastEntry>): List<FastEntry> =
    entries.filter { it.actualHours >= RECENT_MIN_HOURS }
        .sortedByDescending { it.end }
        .take(RECENT_COUNT)

fun longestFasts(entries: List<FastEntry>): List<FastEntry> =
    entries.sortedByDescending { it.actualHours }.take(LONGEST_COUNT)
