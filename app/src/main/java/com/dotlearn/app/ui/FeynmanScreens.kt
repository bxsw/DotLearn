package com.dotlearn.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.dotlearn.app.R
import com.dotlearn.app.audio.AudioPlayer
import com.dotlearn.app.audio.AudioRecorder
import com.dotlearn.app.data.Concept
import com.dotlearn.app.data.FeynmanEntry
import com.dotlearn.app.domain.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeynmanScreen(vm: AppViewModel, nav: NavHostController) {
    val entries by vm.feynmanEntries.collectAsState()
    val conceptsById by vm.conceptsById.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { DotTopBar(title = stringResource(R.string.feynman_title)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(Routes.feynmanEdit(-1L, -1L)) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            DotCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeynmanStep(1, stringResource(R.string.feynman_step1))
                    FeynmanStep(2, stringResource(R.string.feynman_step2))
                    FeynmanStep(3, stringResource(R.string.feynman_step3))
                    FeynmanStep(4, stringResource(R.string.feynman_step4))
                }
            }

            if (entries.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Mic,
                    title = stringResource(R.string.feynman_empty)
                )
            } else {
                entries.forEach { entry ->
                    val concept = entry.conceptId?.let { conceptsById[it] }
                    DotCard(onClick = { nav.navigate(Routes.feynmanEdit(entry.id, -1L)) }) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.conceptName.ifBlank {
                                        concept?.title ?: stringResource(R.string.label_untitled)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (entry.audioPath != null) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            val preview = sequenceOf(entry.explain, entry.simplified)
                                .firstOrNull { it.isNotBlank() }
                            if (preview != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = preview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (entry.blockers.isNotBlank()) {
                                    Pill(
                                        text = stringResource(R.string.feynman_field_blockers),
                                        container = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text = "  ${TimeUtils.formatDate(entry.updatedAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeynmanStep(index: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeynmanEditScreen(
    vm: AppViewModel,
    nav: NavHostController,
    entryId: Long,
    conceptIdArg: Long
) {
    val context = LocalContext.current
    val entries by vm.feynmanEntries.collectAsState()
    val concepts by vm.concepts.collectAsState()
    val subjectsById by vm.subjectsById.collectAsState()

    var currentId by remember(entryId) { mutableLongStateOf(if (entryId > 0L) entryId else 0L) }
    val existing = entries.firstOrNull { it.id == currentId }

    var explain by remember(entryId) { mutableStateOf("") }
    var blockers by remember(entryId) { mutableStateOf("") }
    var simplified by remember(entryId) { mutableStateOf("") }
    var conceptId by remember(entryId) { mutableStateOf(conceptIdArg.takeIf { it > 0 }) }
    var audioPath by remember(entryId) { mutableStateOf<String?>(null) }
    var initialized by remember(entryId) { mutableStateOf(false) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val recorder = remember { AudioRecorder(context.applicationContext) }
    val player = remember { AudioPlayer() }

    LaunchedEffect(currentId, existing) {
        if (!initialized && existing != null) {
            explain = existing.explain
            blockers = existing.blockers
            simplified = existing.simplified
            conceptId = existing.conceptId
            audioPath = existing.audioPath
            initialized = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder.stop() }
            player.stop()
        }
    }

    val selectedConcept: Concept? = conceptId?.let { id -> concepts.firstOrNull { it.id == id } }

    fun persist() {
        val base = existing ?: FeynmanEntry()
        vm.saveFeynman(
            base.copy(
                id = currentId,
                conceptId = conceptId,
                conceptName = selectedConcept?.title ?: "",
                explain = explain.trim(),
                blockers = blockers.trim(),
                simplified = simplified.trim(),
                audioPath = audioPath
            )
        ) { savedId -> currentId = savedId }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val path = recorder.start()
            if (path != null) {
                isRecording = true
                audioPath = path
            } else {
                message = context.getString(R.string.backup_failed)
            }
        } else {
            message = context.getString(R.string.feynman_mic_denied)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = if (entryId < 0L) stringResource(R.string.feynman_new)
                else stringResource(R.string.feynman_edit),
                onBack = {
                    persist()
                    nav.popBackStack()
                },
                actions = {
                    if (currentId > 0L) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            DotCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeynmanStep(1, stringResource(R.string.feynman_step1))
                    FeynmanStep(2, stringResource(R.string.feynman_step2))
                    FeynmanStep(3, stringResource(R.string.feynman_step3))
                    FeynmanStep(4, stringResource(R.string.feynman_step4))
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickerOpen = true }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.feynman_field_concept),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedConcept?.title ?: stringResource(R.string.label_none),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (selectedConcept != null) {
                        val subject = subjectsById[selectedConcept.subjectId]
                        ColorDot(Color(subject?.color ?: 0xFF534AB7.toInt()))
                    }
                }
            }

            LabeledInput(
                label = stringResource(R.string.feynman_field_explain),
                value = explain,
                onValueChange = { explain = it },
                placeholder = stringResource(R.string.feynman_field_explain_hint),
                minLines = 6,
                maxLines = 14
            )

            LabeledInput(
                label = stringResource(R.string.feynman_field_blockers),
                value = blockers,
                onValueChange = { blockers = it },
                placeholder = stringResource(R.string.feynman_field_blockers_hint),
                minLines = 3,
                maxLines = 8
            )

            LabeledInput(
                label = stringResource(R.string.feynman_field_simplified),
                value = simplified,
                onValueChange = { simplified = it },
                placeholder = stringResource(R.string.feynman_field_simplified_hint),
                minLines = 3,
                maxLines = 8
            )

            SectionHeader(text = stringResource(R.string.feynman_record_start))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (isRecording) {
                            val path = recorder.stop()
                            isRecording = false
                            if (path != null) {
                                audioPath = path
                                message = context.getString(R.string.feynman_recording_saved)
                                persist()
                            }
                        } else {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                val path = recorder.start()
                                if (path != null) {
                                    isRecording = true
                                    audioPath = path
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = null
                    )
                    Text(
                        text = if (isRecording) stringResource(R.string.feynman_record_stop)
                        else stringResource(R.string.feynman_record_start),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                val path = audioPath
                if (path != null && !isRecording) {
                    OutlinedButton(
                        onClick = {
                            player.toggle(path) { playing -> isPlaying = playing }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(R.string.feynman_play),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            if (isRecording) {
                Pill(
                    text = stringResource(R.string.feynman_recording),
                    container = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            FilledTonalButton(
                onClick = {
                    persist()
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_save))
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (pickerOpen) {
        ConceptPickerDialog(
            concepts = concepts,
            onDismiss = { pickerOpen = false },
            onPick = { conceptId = it.id }
        )
    }

    if (confirmDelete) {
        val target = existing
        if (target != null) {
            ConfirmDialog(
                title = stringResource(R.string.delete_confirm_title),
                message = stringResource(R.string.feynman_delete_confirm),
                confirmText = stringResource(R.string.action_delete),
                dismissText = stringResource(R.string.action_cancel),
                onConfirm = {
                    vm.deleteFeynman(target)
                    nav.popBackStack()
                },
                onDismiss = { confirmDelete = false }
            )
        }
    }
}
