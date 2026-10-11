package com.dotlearn.app.data

import com.dotlearn.app.domain.SpacedRepetition
import kotlinx.coroutines.flow.Flow

/**
 * 单一数据源：界面层只通过它读写数据，不直接接触 DAO。
 */
class DotLearnRepository(private val db: AppDatabase) {

    private val subjectDao = db.subjectDao()
    private val conceptDao = db.conceptDao()
    private val noteDao = db.cornellNoteDao()
    private val feynmanDao = db.feynmanDao()
    private val chunkDao = db.studyChunkDao()
    private val sessionDao = db.studySessionDao()
    private val reviewDao = db.reviewDao()

    // ---------- 响应式数据流 ----------

    val subjects: Flow<List<Subject>> = subjectDao.observeAll()
    val concepts: Flow<List<Concept>> = conceptDao.observeAll()
    val notes: Flow<List<CornellNote>> = noteDao.observeAll()
    val feynmanEntries: Flow<List<FeynmanEntry>> = feynmanDao.observeAll()
    val chunks: Flow<List<StudyChunk>> = chunkDao.observeAll()
    val sessions: Flow<List<StudySession>> = sessionDao.observeSince(0L)
    val reviews: Flow<List<ReviewSchedule>> = reviewDao.observeAll()

    fun conceptFlow(id: Long): Flow<Concept?> = conceptDao.observeById(id)
    fun notesOfConcept(id: Long): Flow<List<CornellNote>> = noteDao.observeByConcept(id)
    fun feynmanOfConcept(id: Long): Flow<List<FeynmanEntry>> = feynmanDao.observeByConcept(id)
    fun recentSessions(limit: Int = 20): Flow<List<StudySession>> = sessionDao.observeRecent(limit)

    // ---------- 学科 ----------

    suspend fun addSubject(name: String, color: Int): Long =
        subjectDao.insert(Subject(name = name.trim(), color = color))

    suspend fun updateSubject(item: Subject) = subjectDao.update(item)

    suspend fun deleteSubject(item: Subject) = subjectDao.delete(item)

    // ---------- 知识点 ----------

    /** 新建知识点时自动建立第一条复习排程（1 天后）。 */
    suspend fun addConcept(subjectId: Long, title: String, tags: String, mastery: Int): Long {
        val id = conceptDao.insert(
            Concept(
                subjectId = subjectId,
                title = title.trim(),
                tags = tags.trim(),
                mastery = mastery.coerceIn(0, 3)
            )
        )
        reviewDao.insert(
            ReviewSchedule(
                conceptId = id,
                dueAt = SpacedRepetition.dueAtForStage(0),
                stage = 0
            )
        )
        return id
    }

    suspend fun updateConcept(item: Concept) =
        conceptDao.update(item.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteConcept(item: Concept) = conceptDao.delete(item)

    suspend fun getConcept(id: Long): Concept? = conceptDao.getById(id)

    suspend fun setMastery(conceptId: Long, mastery: Int) {
        val c = conceptDao.getById(conceptId) ?: return
        conceptDao.update(
            c.copy(
                mastery = mastery.coerceIn(0, 3),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ---------- 康奈尔笔记 ----------

    suspend fun saveNote(note: CornellNote): Long =
        if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }

    suspend fun getNote(id: Long): CornellNote? = noteDao.getById(id)

    suspend fun deleteNote(note: CornellNote) = noteDao.delete(note)

    // ---------- 费曼讲解 ----------

    suspend fun saveFeynman(item: FeynmanEntry): Long =
        if (item.id == 0L) {
            feynmanDao.insert(item)
        } else {
            feynmanDao.update(item.copy(updatedAt = System.currentTimeMillis()))
            item.id
        }

    suspend fun getFeynman(id: Long): FeynmanEntry? = feynmanDao.getById(id)

    suspend fun deleteFeynman(item: FeynmanEntry) = feynmanDao.delete(item)

    // ---------- 西蒙知识块 ----------

    suspend fun addChunk(subjectId: Long?, title: String, targetMinutes: Int, sortOrder: Int): Long =
        chunkDao.insert(
            StudyChunk(
                subjectId = subjectId,
                title = title.trim(),
                targetMinutes = targetMinutes.coerceIn(5, 240),
                sortOrder = sortOrder
            )
        )

    suspend fun updateChunk(item: StudyChunk) = chunkDao.update(item)

    suspend fun deleteChunk(item: StudyChunk) = chunkDao.delete(item)

    suspend fun getChunk(id: Long): StudyChunk? = chunkDao.getById(id)

    suspend fun chunkCount(): Int = chunkDao.getAll().size

    // ---------- 专注记录 ----------

    suspend fun logSession(session: StudySession): Long = sessionDao.insert(session)

    suspend fun deleteSession(item: StudySession) = sessionDao.delete(item)

    // ---------- 复习排程 ----------

    suspend fun dueReviews(now: Long = System.currentTimeMillis()): List<ReviewSchedule> =
        reviewDao.getDue(now)

    /** 完成一次复习：好则推进阶段，不熟则回到第一阶段，并同步掌握度。 */
    suspend fun review(conceptId: Long, good: Boolean): Int {
        val existing = reviewDao.getByConcept(conceptId)
        val nextStage = SpacedRepetition.nextStage(existing?.stage ?: 0, good)
        val due = SpacedRepetition.dueAtForStage(nextStage)
        val now = System.currentTimeMillis()
        val schedule = if (existing == null) {
            ReviewSchedule(conceptId = conceptId, dueAt = due, stage = nextStage, lastReviewedAt = now)
        } else {
            existing.copy(stage = nextStage, dueAt = due, lastReviewedAt = now)
        }
        reviewDao.insert(schedule)

        val mastery = when {
            nextStage >= SpacedRepetition.MAX_STAGE -> 3
            nextStage >= 2 -> 2
            nextStage >= 1 -> 1
            else -> 0
        }
        setMastery(conceptId, mastery)
        return SpacedRepetition.daysForStage(nextStage)
    }

    // ---------- 备份用 ----------

    suspend fun snapshot(): BackupSnapshot = BackupSnapshot(
        subjects = subjectDao.getAll(),
        concepts = conceptDao.getAll(),
        notes = noteDao.getAll(),
        feynman = feynmanDao.getAll(),
        chunks = chunkDao.getAll(),
        sessions = sessionDao.getAll(),
        reviews = reviewDao.getAll()
    )

    suspend fun restore(snapshot: BackupSnapshot) {
        reviewDao.clear()
        sessionDao.clear()
        feynmanDao.clear()
        noteDao.clear()
        chunkDao.clear()
        conceptDao.clear()
        subjectDao.clear()

        subjectDao.insertAll(snapshot.subjects)
        conceptDao.insertAll(snapshot.concepts)
        noteDao.insertAll(snapshot.notes)
        feynmanDao.insertAll(snapshot.feynman)
        chunkDao.insertAll(snapshot.chunks)
        sessionDao.insertAll(snapshot.sessions)
        reviewDao.insertAll(snapshot.reviews)
    }
}

data class BackupSnapshot(
    val subjects: List<Subject> = emptyList(),
    val concepts: List<Concept> = emptyList(),
    val notes: List<CornellNote> = emptyList(),
    val feynman: List<FeynmanEntry> = emptyList(),
    val chunks: List<StudyChunk> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val reviews: List<ReviewSchedule> = emptyList()
)
