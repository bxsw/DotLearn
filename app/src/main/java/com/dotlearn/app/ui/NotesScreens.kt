package com.dotlearn.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dotlearn.app.R
import com.dotlearn.app.data.Concept
import com.dotlearn.app.data.CornellNote
import com.dotlearn.app.domain.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: AppViewModel, nav: NavHostController) {
    val notes by vm.notes.collectAsState()
    val conceptsById by vm.conceptsById.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = if (query.isBlank()) {
        notes
    } else {
        notes.filter { n ->
            val conceptName = n.conceptId?.let { conceptsById[it]?.title } ?: ""
            listOf(n.title, n.cue, n.notes, n.summary, conceptName)
                .any { it.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { DotTopBar(title = stringResource(R.string.notes_title)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(Routes.noteEdit(-1L, -1L)) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.action_search)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = stringResource(R.string.note_empty)
                )
            } else {
                filtered.forEach { note ->
                    val concept = note.conceptId?.let { conceptsById[it] }
                    NoteRow(note = note, concept = concept) {
                        nav.navigate(Routes.noteEdit(note.id, -1L))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(note: CornellNote, concept: Concept?, onClick: () -> Unit) {
    DotCard(onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.label_untitled) },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (concept != null) {
                    Pill(text = concept.title)
                }
            }
            val preview = sequenceOf(note.cue, note.notes, note.summary)
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
            Text(
                text = TimeUtils.formatDate(note.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    vm: AppViewModel,
    nav: NavHostController,
    noteId: Long,
    conceptIdArg: Long
) {
    val notes by vm.notes.collectAsState()
    val concepts by vm.concepts.collectAsState()
    val subjectsById by vm.subjectsById.collectAsState()

    val existing = notes.firstOrNull { it.id == noteId }

    var initialized by remember(noteId) { mutableStateOf(false) }
    var title by remember(noteId) { mutableStateOf("") }
    var cue by remember(noteId) { mutableStateOf("") }
    var body by remember(noteId) { mutableStateOf("") }
    var summary by remember(noteId) { mutableStateOf("") }
    var conceptId by remember(noteId) {
        mutableStateOf(conceptIdArg.takeIf { it > 0 })
    }
    var pickerOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(noteId, existing) {
        if (!initialized) {
            if (noteId < 0L) {
                initialized = true
            } else if (existing != null) {
                title = existing.title
                cue = existing.cue
                body = existing.notes
                summary = existing.summary
                conceptId = existing.conceptId
                initialized = true
            }
        }
    }

    fun persist() {
        if (title.isBlank() && cue.isBlank() && body.isBlank() && summary.isBlank()) return
        val base = existing ?: CornellNote()
        vm.saveNote(
            base.copy(
                id = if (existing != null) existing.id else 0L,
                conceptId = conceptId,
                title = title.trim(),
                cue = cue.trim(),
                notes = body.trim(),
                summary = summary.trim()
            )
        )
    }

    val selectedConcept = conceptId?.let { id -> concepts.firstOrNull { it.id == id } }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = if (noteId < 0L) stringResource(R.string.note_new)
                else stringResource(R.string.note_edit),
                onBack = {
                    persist()
                    nav.popBackStack()
                },
                actions = {
                    if (noteId > 0L) {
                        IconButton(onClick = {
                            nav.navigate(Routes.noteReview(noteId))
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            LabeledInput(
                label = stringResource(R.string.note_field_title),
                value = title,
                onValueChange = { title = it }
            )

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
                            text = stringResource(R.string.note_linked_concept),
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

            SectionHeader(text = stringResource(R.string.note_field_cue))
            LabeledInput(
                label = stringResource(R.string.note_field_cue),
                value = cue,
                onValueChange = { cue = it },
                placeholder = stringResource(R.string.note_field_cue_hint),
                minLines = 3,
                maxLines = 6
            )

            LabeledInput(
                label = stringResource(R.string.note_field_notes),
                value = body,
                onValueChange = { body = it },
                placeholder = stringResource(R.string.note_field_notes_hint),
                minLines = 8,
                maxLines = 20
            )

            LabeledInput(
                label = stringResource(R.string.note_field_summary),
                value = summary,
                onValueChange = { summary = it },
                placeholder = stringResource(R.string.note_field_summary_hint),
                minLines = 2,
                maxLines = 4
            )

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

    if (confirmDelete && existing != null) {
        ConfirmDialog(
            title = stringResource(R.string.delete_confirm_title),
            message = stringResource(R.string.note_delete_confirm),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                vm.deleteNote(existing)
                nav.popBackStack()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteReviewScreen(vm: AppViewModel, nav: NavHostController, noteId: Long) {
    val notes by vm.notes.collectAsState()
    val note = notes.firstOrNull { it.id == noteId }

    val cues = remember(note?.cue) {
        note?.cue?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val recalled = remember(noteId) { mutableStateListOf<Int>() }
    var notesVisible by remember(noteId) { mutableStateOf(false) }
    var summaryVisible by remember(noteId) { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.note_self_test),
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            Text(
                text = note?.title?.ifBlank { stringResource(R.string.label_untitled) }
                    ?: stringResource(R.string.label_untitled),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.note_self_test_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (cues.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Check,
                    title = stringResource(R.string.note_no_cue)
                )
            } else {
                SectionHeader(
                    text = stringResource(R.string.note_progress, recalled.size, cues.size)
                )
                cues.forEachIndexed { index, line ->
                    val checked = recalled.contains(index)
                    DotCard(
                        onClick = {
                            if (checked) recalled.remove(index) else recalled.add(index)
                        },
                        containerColor = if (checked) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            SectionHeader(
                text = stringResource(R.string.note_field_notes),
                trailing = {
                    TextButton(onClick = { notesVisible = !notesVisible }) {
                        Text(
                            stringResource(
                                if (notesVisible) R.string.note_hide else R.string.note_reveal
                            )
                        )
                    }
                }
            )
            DotCard {
                if (notesVisible) {
                    Text(
                        text = note?.notes?.ifBlank { "-" } ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.note_self_test_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            SectionHeader(
                text = stringResource(R.string.note_field_summary),
                trailing = {
                    TextButton(onClick = { summaryVisible = !summaryVisible }) {
                        Text(
                            stringResource(
                                if (summaryVisible) R.string.note_hide else R.string.note_reveal
                            )
                        )
                    }
                }
            )
            DotCard {
                Text(
                    text = if (summaryVisible) {
                        note?.summary?.ifBlank { "-" } ?: "-"
                    } else {
                        "…"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (note != null && cues.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        recalled.clear()
                        notesVisible = false
                        summaryVisible = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.note_hide_all))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
