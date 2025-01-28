package com.kmptoolkit.cameraxgallery.camera.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import java.io.File


@SuppressLint("MissingPermission")
internal fun startRecording(
    filePath: String,
    videoCapture: VideoCapture<Recorder>,
    context: Context,
    videoRecordingListener: Consumer<VideoRecordEvent>
): Recording {
    val outputOptions = FileOutputOptions.Builder(File(filePath)).build()
    return videoCapture.output
        .prepareRecording(context, outputOptions)
        .apply {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                withAudioEnabled()
            }
        }
        .start(ContextCompat.getMainExecutor(context), videoRecordingListener)
}