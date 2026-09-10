package io.github.anders81fin.nullplate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val heroStats = when {
        fasting -> "${formatHm(elapsed)} / ${formatHours(state.targetHours)}h"
        hasEatingHistory -> "${formatHm(eatingElapsed)} / ${formatHm(eatingTarget)}h"
        else -> ""
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Null Plate",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        HorizontalDivider()

        Text(text = heroTitle, style = MaterialTheme.typography.titleLarge)

        if (heroStats.isNotEmpty()) {
            Text(
                text = heroStats,
                style = MaterialTheme.typography.titleMedium,
                color = if (targetReached) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = heroBlurb,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (fasting) {
            LinearProgressIndicator(
                progress = { progressFraction(elapsed, state.targetHours).toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
        }

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
    }
}
