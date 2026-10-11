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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dotlearn.app.R
import com.dotlearn.app.domain.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(vm: AppViewModel, nav: NavHostController) {
    val stats by vm.todayStats.collectAsState()
    val subjects by vm.subjects.collectAsState()
    val concepts by vm.concepts.collectAsState()
    val subjectsById by vm.subjectsById.collectAsState()

    var showSubjectDialog by remember { mutableStateOf(false) }
    var showSubjectPicker by remember { mutableStateOf(false) }
    var showConceptDialog by remember { mutableStateOf(false) }
    var conceptSubjectId by remember { mutableStateOf(0L) }

    val onNewConcept: () -> Unit = {
        when {
            subjects.isEmpty() -> showSubjectDialog = true
            subjects.size == 1 -> {
                conceptSubjectId = subjects.first().id
                showConceptDialog = true
            }
            else -> showSubjectPicker = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { DotTopBar(title = stringResource(R.string.nav_today)) }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            Column {
                Text(
                    text = stringResource(R.string.today_greeting),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = TimeUtils.formatDate(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = stats.dueCount.toString(),
                    label = stringResource(R.string.today_due_review),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = TimeUtils.formatDuration(stats.todayFocusSec),
                    label = stringResource(R.string.today_focus_today),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary
                )
                StatTile(
                    value = stats.streak.toString(),
                    label = stringResource(R.string.today_streak),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary
                )
            }

            if (stats.dueCount > 0) {
                DotCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.review_due, stats.dueCount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        FilledTonalButton(onClick = { nav.navigate(Routes.REVIEW) }) {
                            Text(stringResource(R.string.today_start_review))
                        }
                    }
                }
            } else {
                DotCard {
                    Text(
                        text = stringResource(R.string.today_no_due),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionHeader(text = stringResource(R.string.nav_today))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.today_quick_new_note),
                    modifier = Modifier.weight(1f)
                ) { nav.navigate(Routes.noteEdit(-1L, -1L)) }
                QuickAction(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.today_quick_start_focus),
                    modifier = Modifier.weight(1f)
                ) { nav.navigate(Routes.FOCUS) }
                QuickAction(
                    icon = Icons.Outlined.Lightbulb,
                    label = stringResource(R.string.today_quick_new_concept),
                    modifier = Modifier.weight(1f)
                ) { onNewConcept() }
            }

            SectionHeader(text = stringResource(R.string.today_recent))

            val recent = concepts.sortedByDescending { it.updatedAt }.take(5)
            if (recent.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.School,
                    title = stringResource(R.string.today_empty_title),
                    description = stringResource(R.string.today_empty_desc)
                )
            } else {
                recent.forEach { concept ->
                    val subject = subjectsById[concept.subjectId]
                    DotCard(
                        onClick = { nav.openConcept(concept.id) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ColorDot(Color(subject?.color ?: 0xFF534AB7.toInt()))
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = concept.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = subject?.name ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Pill(text = masteryLabel(concept.mastery))
                        }
                    }
                }
            }
        }
    }

    if (showSubjectDialog) {
        SubjectEditDialog(
            initial = null,
            onDismiss = { showSubjectDialog = false },
            onSave = { name, color -> vm.addSubject(name, color) }
        )
    }

    if (showSubjectPicker) {
        SubjectPickerDialog(
            subjects = subjects,
            onDismiss = { showSubjectPicker = false },
            onPick = { subject ->
                conceptSubjectId = subject.id
                showConceptDialog = true
            }
        )
    }

    if (showConceptDialog) {
        ConceptEditDialog(
            subjects = subjects,
            initialSubjectId = conceptSubjectId,
            initial = null,
            onDismiss = { showConceptDialog = false },
            onSave = { subjectId, title, tags, mastery ->
                vm.addConcept(subjectId, title, tags, mastery)
            }
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
