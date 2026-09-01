package com.example.recording

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentRecordingId: String? = null,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val errorMessage: String? = null
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(recordingId: String, filePath: String) {
        try {
            stop()
            val file = File(filePath)
            if (!file.exists()) {
                _playbackState.value = PlaybackState(errorMessage = "Recording file not found on device")
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                prepare()
                start()
                _playbackState.value = PlaybackState(
                    isPlaying = true,
                    currentRecordingId = recordingId,
                    durationMs = duration
                )

                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(isPlaying = false, currentPositionMs = 0)
                }

                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState(errorMessage = "Playback error ($what, $extra)")
                    true
                }
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState(errorMessage = "Playback failed: ${e.message}")
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = it.currentPosition
                )
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true
                )
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = PlaybackState()
    }
}
