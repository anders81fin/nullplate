package io.github.anders81fin.nullplate.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.anders81fin.nullplate.MainActivity
import io.github.anders81fin.nullplate.R
import io.github.anders81fin.nullplate.data.FastingStatus
import io.github.anders81fin.nullplate.data.eatingWindowOpen
import io.github.anders81fin.nullplate.domain.eatingStage
import io.github.anders81fin.nullplate.domain.eatingTargetHours
import io.github.anders81fin.nullplate.domain.elapsedHours
import io.github.anders81fin.nullplate.domain.fastingStage
import io.github.anders81fin.nullplate.domain.formatHours

object Notifications {
    // The running timer sits silently in the shade; only the hourly nudges are
    // allowed to make noise.
    private const val CHANNEL_TIMER = "timer"
    private const val CHANNEL_NUDGE = "nudge"

    private const val ID_ONGOING = 1
    private const val ID_NUDGE = 2

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TIMER,
                context.getString(R.string.channel_timer),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NUDGE,
                context.getString(R.string.channel_nudge),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }

    private fun contentIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    // The bar pill's live counter, handed to the system instead of ticked by
    // us: setWhen plus a chronometer makes the platform redraw the elapsed
    // time, so nothing of ours needs to stay awake to keep it moving.
    fun showOngoing(context: Context, status: FastingStatus, now: Long = System.currentTimeMillis() / 1000) {
        val state = status.state
        val fasting = state.fasting
        val anchor = if (fasting) state.startedAt else status.lastEnd
        // A stopped clock has nothing to show; without this the ongoing
        // notification would sit there counting an eating window the user closed.
        if (!fasting && !status.eatingWindowOpen) return

        val elapsed = elapsedHours(anchor, now)
        val title = if (fasting) {
            "Fasting — target ${formatHours(state.targetHours)}h"
        } else {
            "Eating window — ${formatHours(eatingTargetHours(state.targetHours))}h"
        }
        val stage = if (fasting) {
            fastingStage(elapsed)
        } else {
            eatingStage(elapsed, eatingTargetHours(state.targetHours))
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_stat_nullplate)
            .setContentTitle(title)
            .setContentText("${stage.title} — ${stage.blurb}")
            .setWhen(anchor * 1000)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(contentIntent(context))
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(context).notify(ID_ONGOING, notification)
    }

    fun cancelOngoing(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_ONGOING)
    }

    fun showNudge(context: Context, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_NUDGE)
            .setSmallIcon(R.drawable.ic_stat_nullplate)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context))
            .build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(context).notify(ID_NUDGE, notification)
    }
}
