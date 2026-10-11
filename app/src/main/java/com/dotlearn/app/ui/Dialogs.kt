package com.dotlearn.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dotlearn.app.R
import com.dotlearn.app.data.Concept
import com.dotlearn.app.data.StudyChunk
import com.dotlearn.app.data.Subject
import com.dotlearn.app.ui.theme.SubjectPalette

@Composable
fun SubjectEditDialog(
    initial: Subject?,
    onDismiss: () -> Unit,
    onSave: (name: String, color: Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember {
        mutableStateOf(
            initial?.color?.let { Color(it) } ?: SubjectPalette.first()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) stringResource(R.string.subject_add)
                else stringResource(R.string.subject_title)
            )
        },
        text = {
            Column {
                LabeledInput(
                    label = stringResource(R.string.subject_name),
                    value = name,
                    onValueChange = { name = it }
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.subject_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubjectPalette.forEach { c ->
                        val selected = c == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(c, CircleShape)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape
                                )
                                .clickable { color = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), color.toArgb())
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
fun SubjectPickerDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onPick: (Subject) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subject_title)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                items(subjects, key = { it.id }) { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPick(s)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorDot(Color(s.color))
                        Spacer(Modifier.height(0.dp))
                        Text(
                            text = s.name,
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
fun ConceptEditDialog(
    subjects: List<Subject>,
    initialSubjectId: Long,
    initial: Concept?,
    onDismiss: () -> Unit,
    onSave: (subjectId: Long, title: String, tags: String, mastery: Int) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }
    var mastery by remember { mutableStateOf(initial?.mastery ?: 0) }
    var subjectId by remember {
        mutableStateOf(
            initial?.subjectId ?: initialSubjectId.takeIf { it > 0 } ?: subjects.firstOrNull()?.id ?: 0L
        )
    }
    var pickerOpen by remember { mutableStateOf(false) }

    val currentSubject = subjects.firstOrNull { it.id == subjectId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) stringResource(R.string.concept_add)
                else stringResource(R.string.concept_title)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (subjects.size > 1) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickerOpen = true }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorDot(Color(currentSubject?.color ?: 0xFF534AB7.toInt()))
                            Text(
                                text = currentSubject?.name ?: stringResource(R.string.label_none),
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                stringResource(R.string.action_edit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                LabeledInput(
                    label = stringResource(R.string.concept_name),
                    value = title,
                    onValueChange = { title = it }
                )
                Spacer(Modifier.height(12.dp))
                LabeledInput(
                    label = stringResource(R.string.concept_tags),
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = stringResource(R.string.concept_tags_hint)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.concept_mastery),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                MasteryChips(value = mastery, onChange = { mastery = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && subjectId > 0L) {
                        onSave(subjectId, title.trim(), tags.trim(), mastery)
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )

    if (pickerOpen) {
        SubjectPickerDialog(
            subjects = subjects,
            onDismiss = { pickerOpen = false },
            onPick = { subjectId = it.id }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasteryChips(value: Int, onChange: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.concept_mastery_0),
        stringResource(R.string.concept_mastery_1),
        stringResource(R.string.concept_mastery_2),
        stringResource(R.string.concept_mastery_3)
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = value == index,
                onClick = { onChange(index) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun ChunkEditDialog(
    initial: StudyChunk?,
    defaultMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (title: String, minutes: Int) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var minutes by remember { mutableStateOf((initial?.targetMinutes ?: defaultMinutes).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.focus_new_chunk)) },
        text = {
            Column {
                LabeledInput(
                    label = stringResource(R.string.focus_chunk_name),
                    value = title,
                    onValueChange = { title = it }
                )
                Spacer(Modifier.height(12.dp))
                LabeledInput(
                    label = stringResource(R.string.focus_chunk_target),
                    value = minutes,
                    onValueChange = { input -> minutes = input.filter { it.isDigit() }.take(3) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val m = minutes.toIntOrNull() ?: defaultMinutes
                    if (title.isNotBlank()) {
                        onSave(title.trim(), m)
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
fun ConceptPickerDialog(
    concepts: List<Concept>,
    onDismiss: () -> Unit,
    onPick: (Concept) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.concept_title)) },
        text = {
            if (concepts.isEmpty()) {
                Text(
                    stringResource(R.string.concept_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(concepts, key = { it.id }) { c ->
                        Text(
                            text = c.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(c)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        }
    )
}
