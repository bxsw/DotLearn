package com.dotlearn.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dotlearn.app.R

@Composable
fun masteryLabel(mastery: Int): String = stringResource(
    when (mastery) {
        0 -> R.string.concept_mastery_0
        1 -> R.string.concept_mastery_1
        2 -> R.string.concept_mastery_2
        else -> R.string.concept_mastery_3
    }
)

@Composable
fun chunkStatusLabel(status: Int): String = stringResource(
    when (status) {
        0 -> R.string.focus_status_todo
        1 -> R.string.focus_status_doing
        else -> R.string.focus_status_done
    }
)

@Composable
fun timerPhaseLabel(phase: TimerPhase): String = stringResource(
    when (phase) {
        TimerPhase.IDLE -> R.string.focus_timer_idle
        TimerPhase.RUNNING -> R.string.focus_timer_running
        TimerPhase.PAUSED -> R.string.focus_timer_paused
        TimerPhase.FINISHED -> R.string.focus_session_done
    }
)

fun formatClockMmSs(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return String.format("%02d:%02d", m, s)
}
