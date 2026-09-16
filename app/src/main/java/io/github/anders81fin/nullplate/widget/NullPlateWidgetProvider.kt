package io.github.anders81fin.nullplate.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import io.github.anders81fin.nullplate.MainActivity
import io.github.anders81fin.nullplate.R
import io.github.anders81fin.nullplate.data.FastingRepository
import io.github.anders81fin.nullplate.data.FastingStatus
import io.github.anders81fin.nullplate.data.counting
import io.github.anders81fin.nullplate.data.nowEpochSeconds
import io.github.anders81fin.nullplate.domain.PRESETS
import io.github.anders81fin.nullplate.domain.eatingTargetHours
import io.github.anders81fin.nullplate.domain.elapsedHours
import io.github.anders81fin.nullplate.domain.formatHours
import io.github.anders81fin.nullplate.domain.progressFraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NullPlateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val status = FastingRepository(appContext).status.first()
                val views = build(appContext, status)
                ids.forEach { manager.updateAppWidget(it, views) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        fun refresh(context: Context, status: FastingStatus) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, NullPlateWidgetProvider::class.java),
            )
            if (ids.isEmpty()) return

            val views = build(context, status)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun build(context: Context, status: FastingStatus): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_null_plate)
            val state = status.state
            val fasting = state.fasting
            val running = status.counting

            if (running) {
                val anchor = if (fasting) state.startedAt else status.lastEnd
                val now = nowEpochSeconds()
                val target = if (fasting) state.targetHours else eatingTargetHours(state.targetHours)

                // Chronometer counts in the elapsedRealtime timebase, not wall
                // clock, so the start instant has to be rebased on every update
                // — and again after a reboot, when elapsedRealtime restarts.
                val base = SystemClock.elapsedRealtime() - (now - anchor) * 1000

                views.setChronometer(R.id.widget_timer, base, null, true)
                views.setViewVisibility(R.id.widget_timer, View.VISIBLE)
                views.setViewVisibility(R.id.widget_idle, View.GONE)
                views.setTextViewText(
                    R.id.widget_state,
                    if (fasting) "Fasting" else "Eating window",
                )
                views.setTextViewText(R.id.widget_target, "/ ${formatHours(target)}h")
                views.setImageViewBitmap(
                    R.id.widget_ring,
                    ringBitmap(progressFraction(elapsedHours(anchor, now), target).toFloat()),
                )
            } else {
                views.setViewVisibility(R.id.widget_timer, View.GONE)
                views.setViewVisibility(R.id.widget_idle, View.VISIBLE)
                views.setTextViewText(R.id.widget_state, "Ready when you are")
                views.setTextViewText(
                    R.id.widget_idle,
                    PRESETS.firstOrNull { it.hours == state.targetHours }?.label
                        ?: "${formatHours(state.targetHours)}h",
                )
                views.setTextViewText(R.id.widget_target, "")
                views.setImageViewBitmap(R.id.widget_ring, ringBitmap(0f))
            }

            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )

            return views
        }
    }
}
