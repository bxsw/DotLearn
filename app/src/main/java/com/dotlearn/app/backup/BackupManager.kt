package com.dotlearn.app.backup

import android.content.Context
import android.net.Uri
import com.dotlearn.app.data.BackupSnapshot
import com.dotlearn.app.data.Concept
import com.dotlearn.app.data.CornellNote
import com.dotlearn.app.data.DotLearnRepository
import com.dotlearn.app.data.FeynmanEntry
import com.dotlearn.app.data.ReviewSchedule
import com.dotlearn.app.data.StudyChunk
import com.dotlearn.app.data.StudySession
import com.dotlearn.app.data.Subject
import com.dotlearn.app.domain.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 数据导出 / 导入。使用 Android 内置的 org.json，不引入额外依赖。
 * 导出文件为纯文本 JSON，完全本地生成。
 */
class BackupManager(
    private val context: Context,
    private val repo: DotLearnRepository
) {

    suspend fun exportToCache(): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val name = "dotlearn-backup-${TimeUtils.formatDate(System.currentTimeMillis())}.json"
        val file = File(dir, name)
        file.writeText(serialize(repo.snapshot()))
        file
    }

    suspend fun importFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().decodeToString()
            } ?: return@withContext false
            val snapshot = deserialize(text)
            repo.restore(snapshot)
            true
        }.getOrDefault(false)
    }

    // ---------- 序列化 ----------

    private fun serialize(s: BackupSnapshot): String {
        val root = JSONObject()
        root.put("app", "DotLearn")
        root.put("schema", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("subjects", JSONArray().apply {
            s.subjects.forEach { put(jsonSubject(it)) }
        })
        root.put("concepts", JSONArray().apply {
            s.concepts.forEach { put(jsonConcept(it)) }
        })
        root.put("notes", JSONArray().apply {
            s.notes.forEach { put(jsonNote(it)) }
        })
        root.put("feynman", JSONArray().apply {
            s.feynman.forEach { put(jsonFeynman(it)) }
        })
        root.put("chunks", JSONArray().apply {
            s.chunks.forEach { put(jsonChunk(it)) }
        })
        root.put("sessions", JSONArray().apply {
            s.sessions.forEach { put(jsonSession(it)) }
        })
        root.put("reviews", JSONArray().apply {
            s.reviews.forEach { put(jsonReview(it)) }
        })
        return root.toString(2)
    }

    private fun jsonSubject(v: Subject) = JSONObject().apply {
        put("id", v.id); put("name", v.name); put("color", v.color)
        put("sortOrder", v.sortOrder); put("createdAt", v.createdAt)
    }

    private fun jsonConcept(v: Concept) = JSONObject().apply {
        put("id", v.id); put("subjectId", v.subjectId); put("title", v.title)
        put("tags", v.tags); put("mastery", v.mastery)
        put("createdAt", v.createdAt); put("updatedAt", v.updatedAt)
    }

    private fun jsonNote(v: CornellNote) = JSONObject().apply {
        put("id", v.id); put("conceptId", v.conceptId ?: JSONObject.NULL)
        put("title", v.title); put("cue", v.cue); put("notes", v.notes)
        put("summary", v.summary); put("createdAt", v.createdAt); put("updatedAt", v.updatedAt)
    }

    private fun jsonFeynman(v: FeynmanEntry) = JSONObject().apply {
        put("id", v.id); put("conceptId", v.conceptId ?: JSONObject.NULL)
        put("conceptName", v.conceptName); put("explain", v.explain)
        put("blockers", v.blockers); put("simplified", v.simplified)
        put("audioPath", v.audioPath ?: JSONObject.NULL)
        put("createdAt", v.createdAt); put("updatedAt", v.updatedAt)
    }

    private fun jsonChunk(v: StudyChunk) = JSONObject().apply {
        put("id", v.id); put("subjectId", v.subjectId ?: JSONObject.NULL)
        put("title", v.title); put("targetMinutes", v.targetMinutes)
        put("status", v.status); put("sortOrder", v.sortOrder); put("createdAt", v.createdAt)
    }

    private fun jsonSession(v: StudySession) = JSONObject().apply {
        put("id", v.id); put("conceptId", v.conceptId ?: JSONObject.NULL)
        put("chunkId", v.chunkId ?: JSONObject.NULL)
        put("subjectId", v.subjectId ?: JSONObject.NULL)
        put("label", v.label); put("startedAt", v.startedAt)
        put("durationSec", v.durationSec); put("completed", v.completed)
    }

    private fun jsonReview(v: ReviewSchedule) = JSONObject().apply {
        put("id", v.id); put("conceptId", v.conceptId); put("dueAt", v.dueAt)
        put("stage", v.stage); put("lastReviewedAt", v.lastReviewedAt ?: JSONObject.NULL)
    }

    // ---------- 反序列化 ----------

    private fun deserialize(text: String): BackupSnapshot {
        val root = JSONObject(text)

        fun <T> readArray(key: String, mapper: (JSONObject) -> T): List<T> {
            val arr = root.optJSONArray(key) ?: return emptyList()
            val out = ArrayList<T>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                out.add(mapper(obj))
            }
            return out
        }

        return BackupSnapshot(
            subjects = readArray("subjects") { o ->
                Subject(
                    id = o.getLong("id"),
                    name = o.optString("name"),
                    color = o.optInt("color", 0xFF534AB7.toInt()),
                    sortOrder = o.optInt("sortOrder", 0),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            },
            concepts = readArray("concepts") { o ->
                Concept(
                    id = o.getLong("id"),
                    subjectId = o.getLong("subjectId"),
                    title = o.optString("title"),
                    tags = o.optString("tags"),
                    mastery = o.optInt("mastery", 0),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            },
            notes = readArray("notes") { o ->
                CornellNote(
                    id = o.getLong("id"),
                    conceptId = o.optLongOrNull("conceptId"),
                    title = o.optString("title"),
                    cue = o.optString("cue"),
                    notes = o.optString("notes"),
                    summary = o.optString("summary"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            },
            feynman = readArray("feynman") { o ->
                FeynmanEntry(
                    id = o.getLong("id"),
                    conceptId = o.optLongOrNull("conceptId"),
                    conceptName = o.optString("conceptName"),
                    explain = o.optString("explain"),
                    blockers = o.optString("blockers"),
                    simplified = o.optString("simplified"),
                    audioPath = o.optStringOrNull("audioPath"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            },
            chunks = readArray("chunks") { o ->
                StudyChunk(
                    id = o.getLong("id"),
                    subjectId = o.optLongOrNull("subjectId"),
                    title = o.optString("title"),
                    targetMinutes = o.optInt("targetMinutes", 25),
                    status = o.optInt("status", 0),
                    sortOrder = o.optInt("sortOrder", 0),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            },
            sessions = readArray("sessions") { o ->
                StudySession(
                    id = o.getLong("id"),
                    conceptId = o.optLongOrNull("conceptId"),
                    chunkId = o.optLongOrNull("chunkId"),
                    subjectId = o.optLongOrNull("subjectId"),
                    label = o.optString("label"),
                    startedAt = o.optLong("startedAt", System.currentTimeMillis()),
                    durationSec = o.optInt("durationSec", 0),
                    completed = o.optBoolean("completed", true)
                )
            },
            reviews = readArray("reviews") { o ->
                ReviewSchedule(
                    id = o.getLong("id"),
                    conceptId = o.getLong("conceptId"),
                    dueAt = o.optLong("dueAt", System.currentTimeMillis()),
                    stage = o.optInt("stage", 0),
                    lastReviewedAt = o.optLongOrNull("lastReviewedAt")
                )
            }
        )
    }

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (isNull(key)) null else optLong(key, 0L)

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }

    companion object {
        const val SCHEMA_VERSION = 1
    }
}
