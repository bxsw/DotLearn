package com.dotlearn.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dotlearn.app.ServiceLocator
import com.dotlearn.app.backup.BackupManager
import com.dotlearn.app.data.Concept
import com.dotlearn.app.data.CornellNote
import com.dotlearn.app.data.DotLearnRepository
import com.dotlearn.app.data.FeynmanEntry
import com.dotlearn.app.data.ReviewSchedule
import com.dotlearn.app.data.SettingsStore
import com.dotlearn.app.data.StudyChunk
import com.dotlearn.app.data.StudySession
import com.dotlearn.app.data.Subject
import com.dotlearn.app.domain.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerPhase { IDLE, RUNNING, PAUSED, FINISHED }

data class FocusTimerState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val totalSec: Int = 0,
    val remainingSec: Int = 0,
    val label: String = "",
    val chunkId: Long? = null,
    val conceptId: Long? = null,
    val subjectId: Long? = null,
    val startedAt: Long = 0L
)

data class TodayStats(
    val dueCount: Int = 0,
    val todayFocusSec: Int = 0,
    val streak: Int = 0,
    val mastered: Int = 0,
    val totalConcepts: Int = 0
)

data class StudyStats(
    val totalFocusSec: Int = 0,
    val weekFocusSec: Int = 0,
    val noteCount: Int = 0,
    val feynmanCount: Int = 0,
    val conceptCount: Int = 0,
    val mastery: List<Int> = listOf(0, 0, 0, 0),
    val last7Days: List<Int> = List(7) { 0 }
)

/**
 * 应用唯一的共享 ViewModel（Activity 作用域）。
 * 所有界面读取同一份数据流，避免多份状态不同步。
 */
class AppViewModel(
    private val repo: DotLearnRepository,
    private val settings: SettingsStore,
    private val backup: BackupManager
) : ViewModel() {

    private val scope = viewModelScope
    private val started = SharingStarted.WhileSubscribed(5_000)

    // ---------- 原始数据流 ----------

    val subjects: StateFlow<List<Subject>> =
        repo.subjects.stateIn(scope, started, emptyList())

    val concepts: StateFlow<List<Concept>> =
        repo.concepts.stateIn(scope, started, emptyList())

    val notes: StateFlow<List<CornellNote>> =
        repo.notes.stateIn(scope, started, emptyList())

    val feynmanEntries: StateFlow<List<FeynmanEntry>> =
        repo.feynmanEntries.stateIn(scope, started, emptyList())

    val chunks: StateFlow<List<StudyChunk>> =
        repo.chunks.stateIn(scope, started, emptyList())

    val sessions: StateFlow<List<StudySession>> =
        repo.sessions.stateIn(scope, started, emptyList())

    val reviews: StateFlow<List<ReviewSchedule>> =
        repo.reviews.stateIn(scope, started, emptyList())

    // ---------- 设置 ----------

    val pomodoroMinutes: StateFlow<Int> = settings.pomodoroMinutes.stateIn(scope, started, 25)
    val reminderEnabled: StateFlow<Boolean> = settings.reminderEnabled.stateIn(scope, started, false)
    val reminderHour: StateFlow<Int> = settings.reminderHour.stateIn(scope, started, 20)
    val reminderMinute: StateFlow<Int> = settings.reminderMinute.stateIn(scope, started, 0)
    val themeMode: StateFlow<Int> = settings.themeMode.stateIn(scope, started, 0)
    val language: StateFlow<String> = settings.language.stateIn(scope, started, "")

    // ---------- 派生数据 ----------

    val conceptsById: StateFlow<Map<Long, Concept>> = concepts
        .map { list -> list.associateBy { it.id } }
        .stateIn(scope, started, emptyMap())

    val subjectsById: StateFlow<Map<Long, Subject>> = subjects
        .map { list -> list.associateBy { it.id } }
        .stateIn(scope, started, emptyMap())

    /** 今天到期（含已逾期）的复习项。 */
    val dueToday: StateFlow<List<ReviewSchedule>> = reviews
        .map { list ->
            val limit = TimeUtils.tomorrowStart()
            list.filter { it.dueAt < limit }.sortedBy { it.dueAt }
        }
        .stateIn(scope, started, emptyList())

    val todayStats: StateFlow<TodayStats> =
        combine(sessions, concepts, dueToday) { sessionList, conceptList, due ->
            val now = System.currentTimeMillis()
            val todayStart = TimeUtils.todayStart(now)
            TodayStats(
                dueCount = due.size,
                todayFocusSec = sessionList.filter { it.startedAt >= todayStart }
                    .sumOf { it.durationSec },
                streak = TimeUtils.streakDays(sessionList.map { it.startedAt }, now),
                mastered = conceptList.count { it.mastery >= 3 },
                totalConcepts = conceptList.size
            )
        }.stateIn(scope, started, TodayStats())

    val studyStats: StateFlow<StudyStats> =
        combine(sessions, notes, feynmanEntries, concepts) { sessionList, noteList, feynmanList, conceptList ->
            val now = System.currentTimeMillis()
            val todayStart = TimeUtils.todayStart(now)
            val dayMs = TimeUtils.dayMs()
            StudyStats(
                totalFocusSec = sessionList.sumOf { it.durationSec },
                weekFocusSec = sessionList.filter { it.startedAt >= TimeUtils.weekStart(now) }
                    .sumOf { it.durationSec },
                noteCount = noteList.size,
                feynmanCount = feynmanList.size,
                conceptCount = conceptList.size,
                mastery = (0..3).map { level -> conceptList.count { it.mastery == level } },
                last7Days = (6 downTo 0).map { offset ->
                    val from = todayStart - offset * dayMs
                    val to = from + dayMs
                    sessionList.filter { it.startedAt in from until to }
                        .sumOf { it.durationSec }
                }
            )
        }.stateIn(scope, started, StudyStats())

    val recentSessions: StateFlow<List<StudySession>> = sessions
        .map { it.take(20) }
        .stateIn(scope, started, emptyList())

    // ---------- 番茄计时器 ----------

    private val _timer = MutableStateFlow(FocusTimerState())
    val timer: StateFlow<FocusTimerState> = _timer.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(
        totalSec: Int,
        label: String,
        chunkId: Long? = null,
        conceptId: Long? = null,
        subjectId: Long? = null
    ) {
        if (totalSec <= 0) return
        _timer.value = FocusTimerState(
            phase = TimerPhase.RUNNING,
            totalSec = totalSec,
            remainingSec = totalSec,
            label = label,
            chunkId = chunkId,
            conceptId = conceptId,
            subjectId = subjectId,
            startedAt = System.currentTimeMillis()
        )
        runTimer()
    }

    fun pauseTimer() {
        if (_timer.value.phase == TimerPhase.RUNNING) {
            timerJob?.cancel()
            _timer.value = _timer.value.copy(phase = TimerPhase.PAUSED)
        }
    }

    fun resumeTimer() {
        if (_timer.value.phase == TimerPhase.PAUSED) {
            _timer.value = _timer.value.copy(phase = TimerPhase.RUNNING)
            runTimer()
        }
    }

    /** 主动放弃，不记录时长。 */
    fun abortTimer() {
        timerJob?.cancel()
        _timer.value = FocusTimerState()
    }

    /** 关闭完成提示。 */
    fun dismissTimer() {
        timerJob?.cancel()
        _timer.value = FocusTimerState()
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000L)
                val current = _timer.value
                if (current.phase != TimerPhase.RUNNING) break
                val next = current.remainingSec - 1
                if (next <= 0) {
                    _timer.value = current.copy(remainingSec = 0, phase = TimerPhase.FINISHED)
                    recordSession(current, current.totalSec)
                    break
                }
                _timer.value = current.copy(remainingSec = next)
            }
        }
    }

    private fun recordSession(state: FocusTimerState, durationSec: Int) {
        if (durationSec <= 0) return
        scope.launch {
            repo.logSession(
                StudySession(
                    conceptId = state.conceptId,
                    chunkId = state.chunkId,
                    subjectId = state.subjectId,
                    label = state.label,
                    startedAt = if (state.startedAt > 0) state.startedAt else System.currentTimeMillis(),
                    durationSec = durationSec,
                    completed = true
                )
            )
            // 首次专注后把知识块标记为「学习中」
            state.chunkId?.let { id ->
                val chunk = repo.getChunk(id)
                if (chunk != null && chunk.status == 0) {
                    repo.updateChunk(chunk.copy(status = 1))
                }
            }
        }
    }

    // ---------- 学科 ----------

    fun addSubject(name: String, color: Int, onCreated: (Long) -> Unit = {}) {
        if (name.isBlank()) return
        scope.launch { onCreated(repo.addSubject(name, color)) }
    }

    fun updateSubject(item: Subject) = scope.launch { repo.updateSubject(item) }

    fun deleteSubject(item: Subject) = scope.launch { repo.deleteSubject(item) }

    // ---------- 知识点 ----------

    fun addConcept(
        subjectId: Long,
        title: String,
        tags: String,
        mastery: Int,
        onCreated: (Long) -> Unit = {}
    ) {
        if (title.isBlank()) return
        scope.launch { onCreated(repo.addConcept(subjectId, title, tags, mastery)) }
    }

    fun updateConcept(item: Concept) = scope.launch { repo.updateConcept(item) }

    fun deleteConcept(item: Concept) = scope.launch { repo.deleteConcept(item) }

    fun setMastery(conceptId: Long, mastery: Int) =
        scope.launch { repo.setMastery(conceptId, mastery) }

    // ---------- 康奈尔笔记 ----------

    fun saveNote(note: CornellNote) = scope.launch { repo.saveNote(note) }

    fun deleteNote(note: CornellNote) = scope.launch { repo.deleteNote(note) }

    // ---------- 费曼讲解 ----------

    fun saveFeynman(entry: FeynmanEntry, onSaved: (Long) -> Unit = {}) =
        scope.launch { onSaved(repo.saveFeynman(entry)) }

    fun deleteFeynman(entry: FeynmanEntry) = scope.launch { repo.deleteFeynman(entry) }

    // ---------- 西蒙知识块 ----------

    fun addChunk(subjectId: Long?, title: String, targetMinutes: Int) {
        if (title.isBlank()) return
        scope.launch {
            val order = repo.chunkCount()
            repo.addChunk(subjectId, title, targetMinutes, order)
        }
    }

    fun updateChunk(chunk: StudyChunk) = scope.launch { repo.updateChunk(chunk) }

    fun cycleChunkStatus(chunk: StudyChunk) {
        val next = (chunk.status + 1) % 3
        scope.launch { repo.updateChunk(chunk.copy(status = next)) }
    }

    fun deleteChunk(chunk: StudyChunk) = scope.launch { repo.deleteChunk(chunk) }

    // ---------- 复习 ----------

    /** 返回下一次复习的间隔天数。 */
    fun reviewConcept(conceptId: Long, good: Boolean, onDone: (Int) -> Unit = {}) {
        scope.launch { onDone(repo.review(conceptId, good)) }
    }

    // ---------- 设置 ----------

    fun setPomodoroMinutes(minutes: Int) = scope.launch { settings.setPomodoroMinutes(minutes) }
    fun setReminderEnabled(enabled: Boolean) = scope.launch { settings.setReminderEnabled(enabled) }
    fun setReminderTime(hour: Int, minute: Int) = scope.launch { settings.setReminderTime(hour, minute) }
    fun setThemeMode(mode: Int) = scope.launch { settings.setThemeMode(mode) }
    fun setLanguage(tag: String) = scope.launch { settings.setLanguage(tag) }

    // ---------- 备份 ----------

    fun exportBackup(onDone: (java.io.File?) -> Unit) {
        scope.launch { onDone(runCatching { backup.exportToCache() }.getOrNull()) }
    }

    fun importBackup(uri: android.net.Uri, onDone: (Boolean) -> Unit) {
        scope.launch { onDone(backup.importFromUri(uri)) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel(
                    ServiceLocator.repository,
                    ServiceLocator.settings,
                    ServiceLocator.backup
                )
            }
        }
    }
}
