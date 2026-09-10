package io.github.anders81fin.nullplate

import android.app.Application
import io.github.anders81fin.nullplate.notify.Notifications

class NullPlateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
    }
}
