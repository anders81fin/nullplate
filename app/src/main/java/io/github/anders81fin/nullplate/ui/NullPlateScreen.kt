package io.github.anders81fin.nullplate.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.anders81fin.nullplate.R
import io.github.anders81fin.nullplate.domain.PRESETS
import io.github.anders81fin.nullplate.domain.eatingStage
import io.github.anders81fin.nullplate.domain.eatingTargetHours
import io.github.anders81fin.nullplate.domain.elapsedHours
import io.github.anders81fin.nullplate.domain.fastingStage
import io.github.anders81fin.nullplate.domain.formatHm
import io.github.anders81fin.nullplate.domain.formatHours
import io.github.anders81fin.nullplate.domain.progressFraction

@Composable
fun NullPlateScreen(modifier: Modifier = Modifier, viewModel: NullPlateViewModel = viewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::importFrom) }

    val state = status.state
    val fasting = state.fasting
    val elapsed = if (fasting) elapsedHours(state.startedAt, now) else 0.0
    val targetReached = fasting && elapsed >= state.targetHours
    val eatingTarget = eatingTargetHours(state.targetHours)
    val hasEatingHistory = !fasting && status.lastEnd > 0
    val eatingElapsed = if (hasEatingHistory) elapsedHours(status.lastEnd, now) else 0.0

    val heroTitle = when {
        fasting -> "Fasting"
        hasEatingHistory -> "Eating window"
        else -> "Ready when you are"
    }

    val heroBlurb = when {
        fasting -> {
            val stage = fastingStage(elapsed)
            (if (targetReached) "Target smashed! " else "") + "${stage.title} — ${stage.blurb}."
        }
        hasEatingHistory -> {
            val stage = eatingStage(eatingElapsed, eatingTarget)
            "${stage.title} — ${stage.blurb}" + if (status.streak > 0) ". Streak: ${status.streak}." else "."
        }
        else -> "Pick a ratio below and let's go."
    }

    // Idle shows the chosen ratio rather than a 0:00 that looks like a running
    // clock; the empty ring already says nothing has started.
    val ringCenter = when {
        fasting -> formatHm(elapsed)
        hasEatingHistory -> formatHm(eatingElapsed)
        else -> PRESETS.firstOrNull { it.hours == state.targetHours }?.label
            ?: "${formatHours(state.targetHours)}h"
    }

    val ringCaption = when {
        fasting -> "/ ${formatHours(state.targetHours)}h"
        hasEatingHistory -> "/ ${formatHm(eatingTarget)}h"
        else -> null
    }

    val ringProgress = when {
        fasting -> progressFraction(elapsed, state.targetHours)
        hasEatingHistory -> progressFraction(eatingElapsed, eatingTarget)
        else -> 0.0
    }.toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RingHero(
            progress = ringProgress,
            center = ringCenter,
            caption = ringCaption,
            accent = targetReached,
        )

        // The hero block is centred to sit under the ring; the controls below
        // stay left-aligned, because they are a form rather than a headline.
        Text(
            text = heroTitle,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = heroBlurb,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Text(
            text = "FASTING : EATING",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Presets lock while a fast runs, same as the bar widget: changing the
        // ratio mid-fast would move the target the streak is measured against.
        PRESETS.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { preset ->
                    OutlinedButton(
                        onClick = { viewModel.setTarget(preset.hours) },
                        enabled = !fasting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = preset.label,
                            color = if (state.targetHours == preset.hours) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }

        Button(
            onClick = { if (fasting) viewModel.stop() else viewModel.start(state.targetHours) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (fasting) "End fast" else "Start fast (${formatHours(state.targetHours)}h)")
        }

        if (status.recent.isNotEmpty() || status.longest.isNotEmpty()) {
            HorizontalDivider()
        }

        HistorySection(title = "RECENT", entries = status.recent)
        HistorySection(title = "LONGEST", entries = status.longest)

        HorizontalDivider()

        // Nothing else holds a copy of this history, so getting it off the
        // device has to be something the user can actually do.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { exportLauncher.launch("null-plate-backup.json") },
                modifier = Modifier.weight(1f),
            ) {
                Text("Export")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Import")
            }
        }

        // A colophon, not a headline: the name is here to identify the app, and
        // reading from the launcher label keeps it defined in one place.
        Text(
            text = stringResource(R.string.app_name),
            // bodySmall, not labelMedium: label styles carry a medium weight
            // that reads as a heading rather than a signature.
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
    }
}

// The launcher mark doing the app's actual work: an empty plate that fills as
// the fast runs, with the clock in the middle. Replaces a wordmark, a separate
// elapsed-time line and a progress bar with one element.
@Composable
private fun ColumnScope.RingHero(
    progress: Float,
    center: String,
    caption: String?,
    accent: Boolean,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp)
            .size(216.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 14.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = center,
                style = MaterialTheme.typography.displaySmall,
                color = if (accent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
