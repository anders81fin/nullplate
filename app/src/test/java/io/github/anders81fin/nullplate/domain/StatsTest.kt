package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {
    private fun entry(end: Long, actual: Double, target: Double = 16.0) = FastEntry(
        start = end - (actual * 3600).toLong(),
        end = end,
        targetHours = target,
        actualHours = actual,
    )

    @Test
    fun `streak counts successes back from the newest entry`() {
        val entries = listOf(entry(1, 10.0), entry(2, 16.0), entry(3, 17.0))
        assertEquals(2, computeStreak(entries))
    }

    @Test
    fun `streak stops at the first miss from the end`() {
        val entries = listOf(entry(1, 20.0), entry(2, 12.0), entry(3, 18.0))
        assertEquals(1, computeStreak(entries))
    }

    @Test
    fun `empty history has no streak`() {
        assertEquals(0, computeStreak(emptyList()))
    }

    @Test
    fun `hitting the target exactly counts as a success`() {
        assertEquals(1, computeStreak(listOf(entry(1, 16.0, 16.0))))
    }

    @Test
    fun `recent drops fasts shorter than twelve hours`() {
        val entries = listOf(entry(1, 11.9), entry(2, 12.0))
        assertEquals(listOf(2L), recentLongFasts(entries).map { it.end })
    }

    @Test
    fun `recent lists newest first and caps at three`() {
        val entries = listOf(entry(10, 13.0), entry(40, 14.0), entry(20, 15.0), entry(30, 16.0))
        assertEquals(listOf(40L, 30L, 20L), recentLongFasts(entries).map { it.end })
    }

    @Test
    fun `longest ignores the twelve hour floor and ranks by duration`() {
        val entries = listOf(entry(1, 5.0), entry(2, 30.0), entry(3, 9.0))
        assertEquals(listOf(30.0, 9.0, 5.0), longestFasts(entries).map { it.actualHours })
    }
}
