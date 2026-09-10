package io.github.anders81fin.nullplate.notify

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.github.anders81fin.nullplate.data.FastingStatus
import io.github.anders81fin.nullplate.domain.nextHourBoundaryEpochSeconds
import java.util.concurrent.TimeUnit

object NudgeScheduler {
    private const val WORK_NAME = "nullplate-nudge"

    // One request per boundary, re-armed after each firing, rather than a
    // periodic worker: the anchor moves whenever a fast starts or ends, and a
    // periodic schedule would drift away from the whole-hour marks.
    fun scheduleNext(
        context: Context,
        status: FastingStatus,
        now: Long = System.currentTimeMillis() / 1000,
    ) {
        val anchor = when {
            status.state.fasting -> status.state.startedAt
            status.lastEnd > 0 -> status.lastEnd
            else -> {
                cancel(context)
                return
            }
        }

        val delaySeconds = (nextHourBoundaryEpochSeconds(anchor, now) - now).coerceAtLeast(1)
        val request = OneTimeWorkRequestBuilder<NudgeWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
