package io.github.anders81fin.nullplate.data

import android.content.Context
import io.github.anders81fin.nullplate.domain.FastEntry
import io.github.anders81fin.nullplate.domain.FastingState
import io.github.anders81fin.nullplate.domain.completedEntry
import io.github.anders81fin.nullplate.domain.computeStreak
import io.github.anders81fin.nullplate.domain.longestFasts
import io.github.anders81fin.nullplate.domain.recentLongFasts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class FastingStatus(
    val state: FastingState = FastingState(),
    val streak: Int = 0,
    val lastEnd: Long = 0,
    val recent: List<FastEntry> = emptyList(),
    val longest: List<FastEntry> = emptyList(),
)

fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1000

class FastingRepository(context: Context) {
    private val appContext = context.applicationContext

    val status: Flow<FastingStatus> =
        combine(appContext.stateDataStore.data, appContext.historyDataStore.data) { state, entries ->
            FastingStatus(
                state = state,
                streak = computeStreak(entries),
                // The newest entry as written, not the largest end — the Python
                // backend read entries[-1] and the eating window counts from there.
                lastEnd = entries.lastOrNull()?.end ?: 0,
                recent = recentLongFasts(entries),
                longest = longestFasts(entries),
            )
        }

    suspend fun start(targetHours: Double, now: Long = nowEpochSeconds()) {
        appContext.stateDataStore.updateData { state ->
            if (state.fasting) state
            else FastingState(fasting = true, startedAt = now, targetHours = targetHours)
        }
    }

    suspend fun setTarget(targetHours: Double) {
        appContext.stateDataStore.updateData { it.copy(targetHours = targetHours) }
    }

    suspend fun stop(now: Long = nowEpochSeconds()) {
        val state = appContext.stateDataStore.data.first()
        if (!state.fasting) return
        appContext.historyDataStore.updateData { it + completedEntry(state, now) }
        appContext.stateDataStore.updateData { it.copy(fasting = false, startedAt = 0) }
    }
}
