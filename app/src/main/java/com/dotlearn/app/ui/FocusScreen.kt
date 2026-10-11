package com.dotlearn.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dotlearn.app.R
import com.dotlearn.app.data.StudyChunk
import com.dotlearn.app.domain.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(vm: AppViewModel, nav: NavHostController) {
    val chunks by vm.chunks.collectAsState()
    val subjects by vm.subjects.collectAsState()
    val sessions by vm.recentSessions.collectAsState()
    val timer by vm.timer.collectAsState()
    val pomodoro by vm.pomodoroMinutes.collectAsState()
    val todayStats by vm.todayStats.collectAsState()

    var subjectFilter by remember { mutableLongStateOf(-1L) }
    var showChunkDialog by remember { mutableStateOf(false) }
    var editingChunk by remember { mutableStateOf<StudyChunk?>(null) }
    var pendingDelete by remember { mutableStateOf<StudyChunk?>(null) }

    val visibleChunks = chunks.filter { subjectFilter < 0 || it.subjectId == subjectFilter }
    val doneCount = visibleChunks.count { it.status == 2 }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { DotTopBar(title = stringResource(R.string.focus_title)) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingChunk = null
                showChunkDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            Text(
                text = stringResource(
                    R.string.focus_total_today,
                    TimeUtils.formatDuration(todayStats.todayFocusSec)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (timer.phase != TimerPhase.IDLE) {
                TimerCard(
                    phase = timer.phase,
                    remainingSec = timer.remainingSec,
                    totalSec = timer.totalSec,
                    label = timer.label,
                    onPause = { vm.pauseTimer() },
                    onResume = { vm.resumeTimer() },
                    onAbort = { vm.abortTimer() },
                    onDismiss = { vm.dismissTimer() }
                )
            } else if (chunks.isEmpty()) {
                val freeFocusLabel = stringResource(R.string.focus_title)
                DotCard {
                    Column {
                        Text(
                            text = stringResource(R.string.focus_select_block),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = {
                                vm.startTimer(
                                    totalSec = pomodoro * 60,
                                    label = freeFocusLabel
                                )
                            }
                        ) { Text(stringResource(R.string.focus_start)) }
                    }
                }
            }

            SectionHeader(text = stringResource(R.string.focus_progress))

            if (subjects.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = subjectFilter < 0L,
                            onClick = { subjectFilter = -1L },
                            label = { Text(stringResource(R.string.label_all)) }
                        )
                    }
                    items(subjects, key = { it.id }) { s ->
                        FilterChip(
                            selected = subjectFilter == s.id,
                            onClick = { subjectFilter = s.id },
                            label = { Text(s.name) }
                        )
                    }
                }
            }

            if (visibleChunks.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(
                            R.string.focus_progress_value,
                            doneCount,
                            visibleChunks.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (visibleChunks.isEmpty()) 0f
                            else doneCount.toFloat() / visibleChunks.size.toFloat()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (visibleChunks.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Check,
                    title = stringResource(R.string.focus_chunk_empty)
                )
            } else {
                visibleChunks.forEach { chunk ->
                    DotCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable { vm.cycleChunkStatus(chunk) }
                            ) {
                                Text(
                                    text = chunk.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Pill(
                                        text = chunkStatusLabel(chunk.status),
                                        container = when (chunk.status) {
                                            0 -> MaterialTheme.colorScheme.surfaceVariant
                                            1 -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        contentColor = when (chunk.status) {
                                            0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                            1 -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                    Text(
                                        text = "  ${stringResource(R.string.unit_minutes, chunk.targetMinutes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    vm.startTimer(
                                        totalSec = chunk.targetMinutes * 60,
                                        label = chunk.title,
                                        chunkId = chunk.id,
                                        subjectId = chunk.subjectId
                                    )
                                }
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            }
                            IconButton(onClick = {
                                editingChunk = chunk
                                showChunkDialog = true
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = { pendingDelete = chunk }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            SectionHeader(text = stringResource(R.string.focus_records))
            if (sessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.focus_no_records),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sessions.take(8).forEach { session ->
                    DotCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = session.label.ifBlank { stringResource(R.string.focus_title) },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = TimeUtils.formatDate(session.startedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = TimeUtils.formatDuration(session.durationSec),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showChunkDialog) {
        ChunkEditDialog(
            initial = editingChunk,
            defaultMinutes = pomodoro,
            onDismiss = { showChunkDialog = false },
            onSave = { title, minutes ->
                val existing = editingChunk
                if (existing == null) {
                    vm.addChunk(subjectFilter.takeIf { it > 0 }, title, minutes)
                } else {
                    vm.updateChunk(existing.copy(title = title, targetMinutes = minutes))
                }
            }
        )
    }

    pendingDelete?.let { chunk ->
        ConfirmDialog(
            title = stringResource(R.string.delete_confirm_title),
            message = stringResource(R.string.focus_delete_confirm),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { vm.deleteChunk(chunk) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun TimerCard(
    phase: TimerPhase,
    remainingSec: Int,
    totalSec: Int,
    label: String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAbort: () -> Unit,
    onDismiss: () -> Unit
) {
    val progress = if (totalSec <= 0) 0f else remainingSec.toFloat() / totalSec.toFloat()
    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    DotCard {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.ifBlank { stringResource(R.string.focus_title) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.size(184.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 12f
                    val inset = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatClockMmSs(remainingSec),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = timerPhaseLabel(phase),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (phase) {
                    TimerPhase.RUNNING -> {
                        OutlinedButton(onClick = onPause) {
                            Text(stringResource(R.string.focus_pause))
                        }
                        OutlinedButton(onClick = onAbort) {
                            Text(stringResource(R.string.focus_give_up))
                        }
                    }
                    TimerPhase.PAUSED -> {
                        FilledTonalButton(onClick = onResume) {
                            Text(stringResource(R.string.focus_resume))
                        }
                        OutlinedButton(onClick = onAbort) {
                            Text(stringResource(R.string.focus_give_up))
                        }
                    }
                    TimerPhase.FINISHED -> {
                        Text(
                            text = stringResource(R.string.focus_session_saved),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.action_done))
                        }
                    }
                    TimerPhase.IDLE -> Unit
                }
            }
        }
    }
}
