package io.github.anders81fin.nullplate.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.anders81fin.nullplate.data.FastingRepository
import io.github.anders81fin.nullplate.widget.NullPlateWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// WorkManager restores its own enqueued work across a reboot, so this exists
// for the two surfaces the system does not bring back: the ongoing
// notification, and the widget's chronometer, whose elapsedRealtime base is
// meaningless once the clock has restarted.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val status = FastingRepository(appContext).status.first()
                if (status.state.fasting) Notifications.showOngoing(appContext, status)
                NullPlateWidgetProvider.refresh(appContext, status)
            } finally {
                pending.finish()
            }
        }
    }
}
