package io.github.anders81fin.nullplate.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.anders81fin.nullplate.data.FastingRepository
import io.github.anders81fin.nullplate.data.nowEpochSeconds
import io.github.anders81fin.nullplate.domain.eatingHourMessages
import io.github.anders81fin.nullplate.domain.elapsedHours
import io.github.anders81fin.nullplate.domain.fastHourMessages
import io.github.anders81fin.nullplate.widget.NullPlateWidgetProvider
import kotlinx.coroutines.flow.first

class NudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val status = FastingRepository(applicationContext).status.first()
        val now = nowEpochSeconds()

        val anchor = when {
            status.state.fasting -> status.state.startedAt
            status.lastEnd > 0 -> status.lastEnd
            else -> return Result.success()
        }

        // Inexact work can land a little past the mark, so the hour is read off
        // the clock rather than counted — a nudge that fires at 3h00m20s is
        // still the third hour, not the fourth.
        val hours = elapsedHours(anchor, now).toInt()
        if (hours > 0) {
            if (status.state.fasting) {
                Notifications.showNudge(
                    applicationContext,
                    "Null Plate — ${hours}h fasting",
                    fastHourMessages(hours, status.state.targetHours).random(),
                )
            } else {
                Notifications.showNudge(
                    applicationContext,
                    "Null Plate — ${hours}h eating",
                    eatingHourMessages(hours).random(),
                )
            }
            Notifications.showOngoing(applicationContext, status, now)
        }

        // Keeps the widget's progress bar honest; its clock needs no help.
        NullPlateWidgetProvider.refresh(applicationContext, status)
        NudgeScheduler.scheduleNext(applicationContext, status, now)
        return Result.success()
    }
}
