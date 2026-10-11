package com.dotlearn.app.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * 本地录音。文件写入应用私有目录 filesDir/audio，
 * 不涉及任何网络上传。
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    var isRecording: Boolean = false
        private set

    /** 开始录音，返回输出文件路径；失败返回 null。 */
    fun start(): String? {
        if (isRecording) return currentFile?.absolutePath
        return try {
            val dir = File(context.filesDir, "audio").apply { mkdirs() }
            val file = File(dir, "rec_${System.currentTimeMillis()}.m4a")

            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()

            recorder = r
            currentFile = file
            isRecording = true
            file.absolutePath
        } catch (_: Exception) {
            releaseQuietly()
            null
        }
    }

    /** 停止录音，返回保存的文件路径。 */
    fun stop(): String? {
        val r = recorder ?: return null
        val file = currentFile
        return try {
            r.stop()
            file?.absolutePath
        } catch (_: Exception) {
            file?.delete()
            null
        } finally {
            releaseQuietly()
        }
    }

    private fun releaseQuietly() {
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        isRecording = false
    }
}

/** 简单的本地播放器。 */
class AudioPlayer {

    private var player: MediaPlayer? = null

    var playingPath: String? = null
        private set

    fun toggle(path: String, onStateChanged: (Boolean) -> Unit) {
        if (playingPath == path) {
            stop()
            onStateChanged(false)
            return
        }
        stop()
        try {
            val p = MediaPlayer()
            p.setDataSource(path)
            p.setOnCompletionListener {
                stop()
                onStateChanged(false)
            }
            p.prepare()
            p.start()
            player = p
            playingPath = path
            onStateChanged(true)
        } catch (_: Exception) {
            stop()
            onStateChanged(false)
        }
    }

    fun stop() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        playingPath = null
    }
}
