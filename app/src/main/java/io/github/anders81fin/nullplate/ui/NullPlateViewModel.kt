package io.github.anders81fin.nullplate.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.anders81fin.nullplate.data.FastingRepository
import io.github.anders81fin.nullplate.data.BackupFormatException
import io.github.anders81fin.nullplate.data.FastingStatus
import io.github.anders81fin.nullplate.data.MAX_BACKUP_BYTES
import io.github.anders81fin.nullplate.data.counting
import io.github.anders81fin.nullplate.data.decodeBackup
import io.github.anders81fin.nullplate.data.readAtMost
import io.github.anders81fin.nullplate.data.encodeBackup
import io.github.anders81fin.nullplate.data.nowEpochSeconds
import io.github.anders81fin.nullplate.notify.Notifications
import io.github.anders81fin.nullplate.notify.NudgeScheduler
import io.github.anders81fin.nullplate.widget.NullPlateWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
        syncSurfaces()
    }

    fun stop() = viewModelScope.launch {
        repository.stop()
        syncSurfaces()
    }

    /** Stop the clock without logging: discards a fast, or closes the eating window. */
    fun stopCounter() = viewModelScope.launch {
        repository.stopCounter()
        syncSurfaces()
    }

    fun setTarget(targetHours: Double) = viewModelScope.launch {
        repository.setTarget(targetHours)
        syncSurfaces()
    }

    fun exportTo(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>()
        val text = encodeBackup(repository.exportBackup())
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        }.fold(
            onSuccess = { toast("Backup saved") },
            onFailure = { toast("Could not write that file") },
        )
    }

    fun importFrom(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>()
        // A user-supplied file is a real boundary: anything can be in it, at any
        // size. Read a bounded prefix rather than the whole file -- the picker
        // will happily hand over a video if that is what was tapped, and
        // readBytes() on it takes the process down.
        runCatching {
            val text = context.contentResolver.openInputStream(uri)
                ?.use { it.readAtMost(MAX_BACKUP_BYTES) }
                ?: error("unreadable")
            repository.importBackup(decodeBackup(text))
        }.fold(
            onSuccess = {
                syncSurfaces()
                toast("Backup restored")
            },
            // A rejected file is worth explaining when we know why: "too large"
            // and "newer than this app" are both fixable by the user, and both
            // look like a corrupt file if reported as one.
            onFailure = { e ->
                toast(
                    if (e is BackupFormatException) {
                        "Could not import: ${e.message}"
                    } else {
                        "That file is not a Null Plate backup"
                    },
                )
            },
        )
    }

    private suspend fun toast(message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun syncSurfaces() {
        val current = repository.status.first()
        val context = getApplication<Application>()
        NudgeScheduler.scheduleNext(context, current)
        if (current.counting) {
            Notifications.showOngoing(context, current)
        } else {
            Notifications.cancelOngoing(context)
        }
        NullPlateWidgetProvider.refresh(context, current)
    }
}
