package com.dotlearn.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 学科 / 学习项目。所有知识点的顶层归类。
 */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val color: Int = 0xFF534AB7.toInt(),
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 知识点：三种学习法共同的锚点。
 * mastery: 0 未开始 / 1 学习中 / 2 较熟悉 / 3 已掌握
 */
@Entity(
    tableName = "concepts",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class Concept(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectId: Long,
    val title: String,
    val tags: String = "",
    val mastery: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 康奈尔笔记：线索栏 cue / 笔记栏 notes / 总结栏 summary。
 * conceptId 可为空（允许先随手记，之后再关联知识点）。
 */
@Entity(
    tableName = "cornell_notes",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conceptId")]
)
data class CornellNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conceptId: Long? = null,
    val title: String = "",
    val cue: String = "",
    val notes: String = "",
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 费曼讲解记录：讲解内容 / 卡点 / 简化与类比，可选本地语音。
 */
@Entity(
    tableName = "feynman_entries",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conceptId")]
)
data class FeynmanEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conceptId: Long? = null,
    val conceptName: String = "",
    val explain: String = "",
    val blockers: String = "",
    val simplified: String = "",
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 西蒙学习法：把一个大目标拆解成可逐个攻克的知识块。
 * status: 0 未学 / 1 学习中 / 2 已完成
 */
@Entity(
    tableName = "study_chunks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class StudyChunk(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectId: Long? = null,
    val title: String,
    val targetMinutes: Int = 25,
    val status: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 一次专注记录（番茄钟）。label 冗余保存当时的名称，保证历史记录可读。
 */
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conceptId: Long? = null,
    val chunkId: Long? = null,
    val subjectId: Long? = null,
    val label: String = "",
    val startedAt: Long,
    val durationSec: Int,
    val completed: Boolean = true
)

/**
 * 间隔重复复习排程。
 * stage 为 0..4，对应 1 / 3 / 7 / 15 / 30 天。
 */
@Entity(
    tableName = "review_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conceptId")]
)
data class ReviewSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conceptId: Long,
    val dueAt: Long,
    val stage: Int = 0,
    val lastReviewedAt: Long? = null
)
