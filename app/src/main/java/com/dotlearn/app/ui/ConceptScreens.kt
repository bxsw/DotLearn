package com.dotlearn.app.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.dotlearn.app.data.Subject
import com.dotlearn.app.domain.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(vm: AppViewModel, nav: NavHostController) {
    val subjects by vm.subjects.collectAsState()
    val concepts by vm.concepts.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Subject?>(null) }
    var pendingDelete by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.subject_manage),
                onBack = { nav.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            if (subjects.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.School,
                    title = stringResource(R.string.subject_empty)
                )
            } else {
                subjects.forEach { subject ->
                    val count = concepts.count { it.subjectId == subject.id }
                    DotCard(onClick = { nav.navigate(Routes.subjectDetail(subject.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorDot(Color(subject.color), size = 14)
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.subject_count, count),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                editing = subject
                                showDialog = true
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = { pendingDelete = subject }) {
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
        }
    }

    if (showDialog) {
        SubjectEditDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { name, color ->
                val existing = editing
                if (existing == null) {
                    vm.addSubject(name, color)
                } else {
                    vm.updateSubject(existing.copy(name = name, color = color))
                }
            }
        )
    }

    pendingDelete?.let { subject ->
        ConfirmDialog(
            title = stringResource(R.string.delete_confirm_title),
            message = stringResource(R.string.subject_delete_confirm),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { vm.deleteSubject(subject) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(vm: AppViewModel, nav: NavHostController, subjectId: Long) {
    val subjectsById by vm.subjectsById.collectAsState()
    val concepts by vm.concepts.collectAsState()
    val subject = subjectsById[subjectId]
    val list = concepts.filter { it.subjectId == subjectId }.sortedByDescending { it.updatedAt }

    var showConceptDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Concept?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = subject?.name ?: stringResource(R.string.subject_title),
                onBack = { nav.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showConceptDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            SectionHeader(text = stringResource(R.string.concept_title))

            if (list.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Lightbulb,
                    title = stringResource(R.string.concept_empty)
                )
            } else {
                list.forEach { concept ->
                    DotCard(onClick = { nav.openConcept(concept.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = concept.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (concept.tags.isNotBlank()) {
                                    Text(
                                        text = concept.tags,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Pill(text = masteryLabel(concept.mastery))
                            IconButton(onClick = {
                                editing = concept
                                showConceptDialog = true
                            }) {
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConceptDialog) {
        ConceptEditDialog(
            subjects = subjectsById.values.toList(),
            initialSubjectId = subjectId,
            initial = editing,
            onDismiss = { showConceptDialog = false },
            onSave = { sid, title, tags, mastery ->
                val existing = editing
                if (existing == null) {
                    vm.addConcept(sid, title, tags, mastery)
                } else {
                    vm.updateConcept(
                        existing.copy(
                            subjectId = sid,
                            title = title,
                            tags = tags,
                            mastery = mastery
                        )
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConceptDetailScreen(vm: AppViewModel, nav: NavHostController, conceptId: Long) {
    val conceptsById by vm.conceptsById.collectAsState()
    val subjectsById by vm.subjectsById.collectAsState()
    val notes by vm.notes.collectAsState()
    val entries by vm.feynmanEntries.collectAsState()
    val sessions by vm.sessions.collectAsState()

    val concept = conceptsById[conceptId]
    val subject = concept?.let { subjectsById[it.subjectId] }

    val conceptNotes = notes.filter { it.conceptId == conceptId }
    val conceptEntries = entries.filter { it.conceptId == conceptId }
    val conceptSessions = sessions.filter { it.conceptId == conceptId }.take(5)

    var showEdit by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.concept_detail),
                onBack = { nav.popBackStack() },
                actions = {
                    IconButton(onClick = { showEdit = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            if (concept == null) {
                EmptyState(
                    icon = Icons.Outlined.Lightbulb,
                    title = stringResource(R.string.concept_empty)
                )
            } else {
                DotCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorDot(Color(subject?.color ?: 0xFF534AB7.toInt()))
                            Text(
                                text = subject?.name ?: stringResource(R.string.subject_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = concept.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        if (concept.tags.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = concept.tags,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SectionHeader(text = stringResource(R.string.concept_mastery))
                MasteryChips(
                    value = concept.mastery,
                    onChange = { vm.setMastery(concept.id, it) }
                )

                SectionHeader(
                    text = stringResource(R.string.concept_section_notes),
                    trailing = {
                        TextButton(onClick = {
                            nav.navigate(Routes.noteEdit(-1L, concept.id))
                        }) { Text(stringResource(R.string.action_add)) }
                    }
                )
                if (conceptNotes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.concept_no_notes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    conceptNotes.forEach { note ->
                        DotCard(onClick = { nav.navigate(Routes.noteEdit(note.id, -1L)) }) {
                            Column {
                                Text(
                                    text = note.title.ifBlank {
                                        stringResource(R.string.label_untitled)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                val preview = sequenceOf(note.cue, note.notes)
                                    .firstOrNull { it.isNotBlank() }
                                if (preview != null) {
                                    Text(
                                        text = preview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                SectionHeader(
                    text = stringResource(R.string.concept_section_feynman),
                    trailing = {
                        TextButton(onClick = {
                            nav.navigate(Routes.feynmanEdit(-1L, concept.id))
                        }) { Text(stringResource(R.string.action_add)) }
                    }
                )
                if (conceptEntries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.concept_no_feynman),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    conceptEntries.forEach { entry ->
                        DotCard(onClick = {
                            nav.navigate(Routes.feynmanEdit(entry.id, -1L))
                        }) {
                            Column {
                                val preview = sequenceOf(entry.simplified, entry.explain)
                                    .firstOrNull { it.isNotBlank() }
                                Text(
                                    text = preview ?: stringResource(R.string.label_untitled),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = TimeUtils.formatDate(entry.updatedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                SectionHeader(text = stringResource(R.string.concept_section_sessions))
                if (conceptSessions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.concept_no_sessions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    conceptSessions.forEach { session ->
                        DotCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = TimeUtils.formatDate(session.startedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = TimeUtils.formatDuration(session.durationSec),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                FilledTonalButton(
                    onClick = {
                        vm.startTimer(
                            totalSec = 25 * 60,
                            label = concept.title,
                            conceptId = concept.id,
                            subjectId = concept.subjectId
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.today_quick_start_focus))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showEdit && concept != null) {
        ConceptEditDialog(
            subjects = subjectsById.values.toList(),
            initialSubjectId = concept.subjectId,
            initial = concept,
            onDismiss = { showEdit = false },
            onSave = { sid, title, tags, mastery ->
                vm.updateConcept(
                    concept.copy(
                        subjectId = sid,
                        title = title,
                        tags = tags,
                        mastery = mastery
                    )
                )
            }
        )
    }

    if (confirmDelete && concept != null) {
        ConfirmDialog(
            title = stringResource(R.string.delete_confirm_title),
            message = stringResource(R.string.concept_delete_confirm),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                vm.deleteConcept(concept)
                nav.popBackStack()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
