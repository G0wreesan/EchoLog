package com.echolog.app.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun startRecording(outputFile: File) {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
    }
}