package com.dotlearn.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<Subject>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Insert
    suspend fun insert(item: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Subject>)

    @Update
    suspend fun update(item: Subject)

    @Delete
    suspend fun delete(item: Subject)

    @Query("DELETE FROM subjects")
    suspend fun clear()
}

@Dao
interface ConceptDao {
    @Query("SELECT * FROM concepts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    fun observeBySubject(subjectId: Long): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE id = :id")
    fun observeById(id: Long): Flow<Concept?>

    @Query("SELECT * FROM concepts WHERE id = :id")
    suspend fun getById(id: Long): Concept?

    @Query("SELECT * FROM concepts ORDER BY updatedAt DESC")
    suspend fun getAll(): List<Concept>

    @Insert
    suspend fun insert(item: Concept): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Concept>)

    @Update
    suspend fun update(item: Concept)

    @Delete
    suspend fun delete(item: Concept)

    @Query("DELETE FROM concepts")
    suspend fun clear()
}

@Dao
interface CornellNoteDao {
    @Query("SELECT * FROM cornell_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CornellNote>>

    @Query("SELECT * FROM cornell_notes WHERE conceptId = :conceptId ORDER BY updatedAt DESC")
    fun observeByConcept(conceptId: Long): Flow<List<CornellNote>>

    @Query("SELECT * FROM cornell_notes WHERE id = :id")
    suspend fun getById(id: Long): CornellNote?

    @Query("SELECT * FROM cornell_notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CornellNote>

    @Insert
    suspend fun insert(item: CornellNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CornellNote>)

    @Update
    suspend fun update(item: CornellNote)

    @Delete
    suspend fun delete(item: CornellNote)

    @Query("DELETE FROM cornell_notes")
    suspend fun clear()
}

@Dao
interface FeynmanDao {
    @Query("SELECT * FROM feynman_entries ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<FeynmanEntry>>

    @Query("SELECT * FROM feynman_entries WHERE conceptId = :conceptId ORDER BY updatedAt DESC")
    fun observeByConcept(conceptId: Long): Flow<List<FeynmanEntry>>

    @Query("SELECT * FROM feynman_entries WHERE id = :id")
    suspend fun getById(id: Long): FeynmanEntry?

    @Query("SELECT * FROM feynman_entries ORDER BY updatedAt DESC")
    suspend fun getAll(): List<FeynmanEntry>

    @Insert
    suspend fun insert(item: FeynmanEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FeynmanEntry>)

    @Update
    suspend fun update(item: FeynmanEntry)

    @Delete
    suspend fun delete(item: FeynmanEntry)

    @Query("DELETE FROM feynman_entries")
    suspend fun clear()
}

@Dao
interface StudyChunkDao {
    @Query("SELECT * FROM study_chunks ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<StudyChunk>>

    @Query("SELECT * FROM study_chunks WHERE subjectId = :subjectId ORDER BY sortOrder ASC, id ASC")
    fun observeBySubject(subjectId: Long): Flow<List<StudyChunk>>

    @Query("SELECT * FROM study_chunks ORDER BY sortOrder ASC, id ASC")
    suspend fun getAll(): List<StudyChunk>

    @Query("SELECT * FROM study_chunks WHERE id = :id")
    suspend fun getById(id: Long): StudyChunk?

    @Insert
    suspend fun insert(item: StudyChunk): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StudyChunk>)

    @Update
    suspend fun update(item: StudyChunk)

    @Delete
    suspend fun delete(item: StudyChunk)

    @Query("DELETE FROM study_chunks")
    suspend fun clear()
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE startedAt >= :from ORDER BY startedAt DESC")
    fun observeSince(from: Long): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions ORDER BY startedAt DESC")
    suspend fun getAll(): List<StudySession>

    @Insert
    suspend fun insert(item: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StudySession>)

    @Delete
    suspend fun delete(item: StudySession)

    @Query("DELETE FROM study_sessions")
    suspend fun clear()
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_schedules WHERE dueAt <= :now ORDER BY dueAt ASC")
    fun observeDue(now: Long): Flow<List<ReviewSchedule>>

    @Query("SELECT * FROM review_schedules ORDER BY dueAt ASC")
    fun observeAll(): Flow<List<ReviewSchedule>>

    @Query("SELECT * FROM review_schedules WHERE conceptId = :conceptId LIMIT 1")
    suspend fun getByConcept(conceptId: Long): ReviewSchedule?

    @Query("SELECT * FROM review_schedules")
    suspend fun getAll(): List<ReviewSchedule>

    @Query("SELECT * FROM review_schedules WHERE dueAt <= :now ORDER BY dueAt ASC")
    suspend fun getDue(now: Long): List<ReviewSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReviewSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReviewSchedule>)

    @Update
    suspend fun update(item: ReviewSchedule)

    @Delete
    suspend fun delete(item: ReviewSchedule)

    @Query("DELETE FROM review_schedules")
    suspend fun clear()
}
