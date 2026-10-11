package com.dotlearn.app.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import com.dotlearn.app.R
import com.dotlearn.app.data.Concept
import com.dotlearn.app.domain.TimeUtils
import com.dotlearn.app.work.ReminderScheduler
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController) {
    val stats by vm.studyStats.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { DotTopBar(title = stringResource(R.string.nav_me)) }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            DotCard {
                Row {
                    StatTile(
                        value = TimeUtils.formatDuration(stats.totalFocusSec),
                        label = stringResource(R.string.stats_total_focus),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.size(10.dp))
                    StatTile(
                        value = stats.conceptCount.toString(),
                        label = stringResource(R.string.stats_concepts_count),
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            DotCard(onClick = { nav.navigate(Routes.STATS) }) {
                MenuRow(
                    icon = Icons.Outlined.BarChart,
                    title = stringResource(R.string.stats_title)
                )
            }
            DotCard(onClick = { nav.navigate(Routes.SUBJECTS) }) {
                MenuRow(
                    icon = Icons.Outlined.School,
                    title = stringResource(R.string.subject_manage)
                )
            }
            DotCard(onClick = { nav.navigate(Routes.SETTINGS) }) {
                MenuRow(
                    icon = Icons.Filled.Settings,
                    title = stringResource(R.string.settings_title)
                )
            }

            DotCard {
                Column {
                    Text(
                        text = stringResource(R.string.settings_offline_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_offline_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: AppViewModel, nav: NavHostController) {
    val stats by vm.studyStats.collectAsState()
    val todayStats by vm.todayStats.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.stats_title),
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = TimeUtils.formatDuration(stats.totalFocusSec),
                    label = stringResource(R.string.stats_total_focus),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = TimeUtils.formatDuration(stats.weekFocusSec),
                    label = stringResource(R.string.stats_week_focus),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = todayStats.streak.toString(),
                    label = stringResource(R.string.stats_streak),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary
                )
                StatTile(
                    value = stats.noteCount.toString(),
                    label = stringResource(R.string.stats_notes_count),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = stats.feynmanCount.toString(),
                    label = stringResource(R.string.stats_feynman_count),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary
                )
            }

            SectionHeader(text = stringResource(R.string.stats_last_7_days))
            DotCard {
                if (stats.last7Days.all { it == 0 }) {
                    Text(
                        text = stringResource(R.string.chart_no_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    WeekChart(values = stats.last7Days, refNow = System.currentTimeMillis())
                }
            }

            SectionHeader(text = stringResource(R.string.stats_mastery_distribution))
            DotCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val total = stats.conceptCount.coerceAtLeast(1)
                    stats.mastery.forEachIndexed { level, count ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = masteryLabel(level),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / total.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekChart(values: List<Int>, refNow: Long) {
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val labels = listOf(
        stringResource(R.string.weekday_mon),
        stringResource(R.string.weekday_tue),
        stringResource(R.string.weekday_wed),
        stringResource(R.string.weekday_thu),
        stringResource(R.string.weekday_fri),
        stringResource(R.string.weekday_sat),
        stringResource(R.string.weekday_sun)
    )
    val todayIndex = remember(refNow) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = refNow
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) 6 else dow - 2
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.62f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(trackColor),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fraction = if (value <= 0) 0f
                    else (value.toFloat() / maxValue.toFloat()).coerceIn(0.08f, 1f)
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (index == todayIndex) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        barColor
                                    }
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = labels[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == todayIndex) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val language by vm.language.collectAsState()
    val themeMode by vm.themeMode.collectAsState()
    val pomodoro by vm.pomodoroMinutes.collectAsState()
    val reminderEnabled by vm.reminderEnabled.collectAsState()
    val reminderHour by vm.reminderHour.collectAsState()
    val reminderMinute by vm.reminderMinute.collectAsState()

    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val versionName = remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "1.0"
        }.getOrNull() ?: "1.0"
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            showImportConfirm = true
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    fun applyReminder(enabled: Boolean, hour: Int, minute: Int) {
        if (enabled) {
            ReminderScheduler.scheduleAt(context, hour, minute)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.settings_title),
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            SectionHeader(text = stringResource(R.string.settings_language))
            DotCard {
                Column {
                    val optionSystem = stringResource(R.string.settings_language_system)
                    val optionZh = stringResource(R.string.settings_language_zh)
                    val optionEn = stringResource(R.string.settings_language_en)
                    RadioRow(
                        label = optionSystem,
                        selected = language.isEmpty(),
                        onClick = {
                            vm.setLanguage("")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.getEmptyLocaleList()
                            )
                        }
                    )
                    RadioRow(
                        label = optionZh,
                        selected = language == "zh",
                        onClick = {
                            vm.setLanguage("zh")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("zh")
                            )
                        }
                    )
                    RadioRow(
                        label = optionEn,
                        selected = language == "en",
                        onClick = {
                            vm.setLanguage("en")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("en")
                            )
                        }
                    )
                }
            }

            SectionHeader(text = stringResource(R.string.settings_theme))
            DotCard {
                Column {
                    val optSystem = stringResource(R.string.settings_theme_system)
                    val optLight = stringResource(R.string.settings_theme_light)
                    val optDark = stringResource(R.string.settings_theme_dark)
                    RadioRow(optSystem, themeMode == 0) { vm.setThemeMode(0) }
                    RadioRow(optLight, themeMode == 1) { vm.setThemeMode(1) }
                    RadioRow(optDark, themeMode == 2) { vm.setThemeMode(2) }
                }
            }

            SectionHeader(text = stringResource(R.string.settings_study))
            DotCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.focus_pomodoro_len),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Stepper(
                            value = pomodoro,
                            onDecrease = { vm.setPomodoroMinutes(pomodoro - 5) },
                            onIncrease = { vm.setPomodoroMinutes(pomodoro + 5) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.settings_reminder),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { checked ->
                                vm.setReminderEnabled(checked)
                                applyReminder(checked, reminderHour, reminderMinute)
                            }
                        )
                    }
                    if (reminderEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        vm.setReminderTime(h, m)
                                        applyReminder(true, h, m)
                                    },
                                    reminderHour,
                                    reminderMinute,
                                    true
                                ).show()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.settings_reminder_time),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = String.format(
                                    "%02d:%02d",
                                    reminderHour,
                                    reminderMinute
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_reminder_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    notificationLauncher.launch(
                                        android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.settings_permission_notifications))
                            }
                        }
                    }
                }
            }

            SectionHeader(text = stringResource(R.string.settings_data))
            DotCard {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.exportBackup { file ->
                                    if (file != null) {
                                        runCatching {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(
                                                    intent,
                                                    context.getString(R.string.backup_share)
                                                )
                                            )
                                        }
                                        message = context.getString(R.string.backup_export_success)
                                    } else {
                                        message = context.getString(R.string.backup_failed)
                                    }
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.backup_export),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Upload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.backup_import),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            SectionHeader(text = stringResource(R.string.settings_about))
            DotCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.settings_version),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = versionName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_offline_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showImportConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.backup_import),
            message = stringResource(R.string.backup_import_confirm),
            confirmText = stringResource(R.string.action_confirm),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                val uri = pendingUri
                if (uri != null) {
                    vm.importBackup(uri) { ok ->
                        message = context.getString(
                            if (ok) R.string.backup_import_success else R.string.backup_failed
                        )
                    }
                }
            },
            onDismiss = {
                showImportConfirm = false
                pendingUri = null
            }
        )
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Stepper(value: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onDecrease) { Text("-") }
        Text(
            text = stringResource(R.string.unit_minutes, value),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        TextButton(onClick = onIncrease) { Text("+") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(vm: AppViewModel, nav: NavHostController) {
    val due by vm.dueToday.collectAsState()
    val conceptsById by vm.conceptsById.collectAsState()
    val notes by vm.notes.collectAsState()

    var queue by remember { mutableStateOf<List<Concept>>(emptyList()) }
    var captured by remember { mutableStateOf(false) }
    var index by remember { mutableIntStateOf(0) }
    var lastDays by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(due, conceptsById) {
        if (!captured) {
            if (due.isNotEmpty()) {
                val list = due.mapNotNull { conceptsById[it.conceptId] }
                if (list.isNotEmpty()) {
                    queue = list
                    captured = true
                }
            } else if (conceptsById.isNotEmpty()) {
                captured = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DotTopBar(
                title = stringResource(R.string.review_title),
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        ScreenColumn(Modifier.padding(pad)) {

            val current = queue.getOrNull(index)

            if (queue.isEmpty() || current == null) {
                EmptyState(
                    icon = Icons.Filled.Check,
                    title = stringResource(R.string.review_empty)
                )
                if (queue.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.review_finish))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.review_stage, index + 1, queue.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { (index).toFloat() / queue.size.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )

                DotCard {
                    Column {
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Pill(text = masteryLabel(current.mastery))
                    }
                }

                val cueNote = notes
                    .filter { it.conceptId == current.id && it.cue.isNotBlank() }
                    .maxByOrNull { it.updatedAt }
                if (cueNote != null) {
                    SectionHeader(text = stringResource(R.string.note_field_cue))
                    DotCard {
                        Text(
                            text = cueNote.cue,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                lastDays?.let {
                    Text(
                        text = stringResource(R.string.review_next_due, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            vm.reviewConcept(current.id, false) { days -> lastDays = days }
                            index += 1
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.review_again))
                    }
                    FilledTonalButton(
                        onClick = {
                            vm.reviewConcept(current.id, true) { days -> lastDays = days }
                            index += 1
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.review_good))
                    }
                }
            }
        }
    }
}
