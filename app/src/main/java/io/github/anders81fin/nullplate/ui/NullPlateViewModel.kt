package io.github.anders81fin.nullplate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.anders81fin.nullplate.data.FastingRepository
import io.github.anders81fin.nullplate.data.FastingStatus
import io.github.anders81fin.nullplate.data.nowEpochSeconds
import io.github.anders81fin.nullplate.notify.Notifications
import io.github.anders81fin.nullplate.notify.NudgeScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NullPlateViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = FastingRepository(app)

    val status: StateFlow<FastingStatus> = repository.status
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FastingStatus())

    // Ticks only while the screen collects it. Nothing depends on this flow to
    // keep time in the background — the shade's chronometer does that.
    val now: StateFlow<Long> = flow {
        while (true) {
            emit(nowEpochSeconds())
            delay(1_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), nowEpochSeconds())

    fun start(targetHours: Double) = viewModelScope.launch {
        repository.start(targetHours)
        syncNotifications()
    }

    fun stop() = viewModelScope.launch {
        repository.stop()
        syncNotifications()
    }

    fun setTarget(targetHours: Double) = viewModelScope.launch {
        repository.setTarget(targetHours)
    }

    private suspend fun syncNotifications() {
        val current = repository.status.first()
        val context = getApplication<Application>()
        NudgeScheduler.scheduleNext(context, current)
        if (current.state.fasting || current.lastEnd > 0) {
            Notifications.showOngoing(context, current)
        } else {
            Notifications.cancelOngoing(context)
        }
    }
}
