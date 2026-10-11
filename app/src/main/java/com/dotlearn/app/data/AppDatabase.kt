package com.dotlearn.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Subject::class,
        Concept::class,
        CornellNote::class,
        FeynmanEntry::class,
        StudyChunk::class,
        StudySession::class,
        ReviewSchedule::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun conceptDao(): ConceptDao
    abstract fun cornellNoteDao(): CornellNoteDao
    abstract fun feynmanDao(): FeynmanDao
    abstract fun studyChunkDao(): StudyChunkDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dotlearn.db"
                ).build().also { INSTANCE = it }
            }
    }
}
