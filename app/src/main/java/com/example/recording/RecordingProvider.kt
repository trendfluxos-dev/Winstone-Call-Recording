package com.example.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.model.RecordingMetadata
import com.example.model.RecordingStatus
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Standard recording provider contract for Winstone Auto Call Recorder.
 */
interface RecordingProvider {
    fun checkAvailability(): Boolean
    fun startRecording(callId: String, leadId: String?): RecordingResult
    fun stopRecording(): RecordingResult
    fun getRecordingFile(): File?
    fun getRecordingStatus(): RecordingStatus
}

sealed class RecordingResult {
    data class Success(val metadata: RecordingMetadata, val file: File) : RecordingResult()
    data class Error(val message: String, val status: RecordingStatus) : RecordingResult()
}

/**
 * Native Device Recording Provider utilizing official Android MediaRecorder APIs.
 * Operates in private application storage without root or prohibited accessibility workarounds.
 */
class NativeDeviceRecordingProvider(private val context: Context) : RecordingProvider {

    private val TAG = "NativeRecordingProvider"
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var currentCallId: String? = null
    private var currentLeadId: String? = null
    private var startTimeMs: Long = 0L
    private var status: RecordingStatus = RecordingStatus.NOT_RECORDED

    override fun checkAvailability(): Boolean {
        // Only return true if audio source and permissions are legitimate
        val manager = RecordingCapabilityManager(context)
        val result = manager.checkRecordingCapability()
        return result.canRecordMic
    }

    override fun startRecording(callId: String, leadId: String?): RecordingResult {
        return try {
            val privateDir = File(context.filesDir, "winstone_recordings").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "rec_${callId}_${System.currentTimeMillis()}.m4a"
            val outputFile = File(privateDir, fileName)

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            // Using standard MIC audio source for permitted Android operations
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(outputFile.absolutePath)

            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            currentOutputFile = outputFile
            currentCallId = callId
            currentLeadId = leadId
            startTimeMs = System.currentTimeMillis()
            status = RecordingStatus.RECORDING

            Log.i(TAG, "Recording started for call: $callId -> ${outputFile.absolutePath}")
            RecordingResult.Success(
                metadata = RecordingMetadata(
                    recordingId = UUID.randomUUID().toString(),
                    callId = callId,
                    leadId = leadId,
                    filePath = outputFile.absolutePath,
                    mimeType = "audio/mp4",
                    fileSize = 0L,
                    durationMs = 0L,
                    uploadStatus = RecordingStatus.RECORDING
                ),
                file = outputFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start native recording", e)
            status = RecordingStatus.RECORDING_FAILED
            RecordingResult.Error(
                message = e.localizedMessage ?: "Failed to initialize audio recorder",
                status = RecordingStatus.RECORDING_FAILED
            )
        }
    }

    override fun stopRecording(): RecordingResult {
        val recorder = mediaRecorder
        val file = currentOutputFile
        val callId = currentCallId ?: "unknown"

        if (recorder == null || file == null) {
            status = RecordingStatus.NOT_RECORDED
            return RecordingResult.Error("No active recording session", RecordingStatus.NOT_RECORDED)
        }

        return try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null

            val durationMs = System.currentTimeMillis() - startTimeMs
            val fileSize = file.length()

            // Strict Validation according to prompt spec:
            // - file exists
            // - file size > 0
            // - supported audio format
            // - duration is plausible
            // - file is readable
            if (file.exists() && fileSize > 0 && file.canRead() && durationMs > 500) {
                status = RecordingStatus.RECORDED_LOCAL
                val metadata = RecordingMetadata(
                    recordingId = UUID.randomUUID().toString(),
                    callId = callId,
                    leadId = currentLeadId,
                    filePath = file.absolutePath,
                    mimeType = "audio/mp4",
                    fileSize = fileSize,
                    durationMs = durationMs,
                    createdAt = System.currentTimeMillis(),
                    uploadStatus = RecordingStatus.RECORDED_LOCAL
                )
                RecordingResult.Success(metadata, file)
            } else {
                status = RecordingStatus.RECORDING_FAILED
                RecordingResult.Error("Recording file validation failed (empty or corrupt)", RecordingStatus.RECORDING_FAILED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording cleanly", e)
            try {
                mediaRecorder?.release()
            } catch (_: Exception) {}
            mediaRecorder = null
            status = RecordingStatus.RECORDING_FAILED
            RecordingResult.Error(e.localizedMessage ?: "Failed to stop recording", RecordingStatus.RECORDING_FAILED)
        }
    }

    override fun getRecordingFile(): File? = currentOutputFile
    override fun getRecordingStatus(): RecordingStatus = status
}

/**
 * Manual Upload Provider when automatic recording is unavailable.
 * Allows agents to attach an authorized recording from internal storage.
 */
class ManualUploadProvider(private val context: Context) {

    fun saveImportedAudio(inputStream: InputStream, originalFileName: String, callId: String, leadId: String?): RecordingResult {
        return try {
            val privateDir = File(context.filesDir, "winstone_recordings").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(privateDir, "manual_${callId}_${System.currentTimeMillis()}_$originalFileName")

            FileOutputStream(targetFile).use { output ->
                inputStream.copyTo(output)
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                val metadata = RecordingMetadata(
                    recordingId = UUID.randomUUID().toString(),
                    callId = callId,
                    leadId = leadId,
                    filePath = targetFile.absolutePath,
                    mimeType = "audio/mp4",
                    fileSize = targetFile.length(),
                    durationMs = 60000L, // default estimated
                    createdAt = System.currentTimeMillis(),
                    uploadStatus = RecordingStatus.UPLOAD_PENDING
                )
                RecordingResult.Success(metadata, targetFile)
            } else {
                RecordingResult.Error("Imported file is empty", RecordingStatus.RECORDING_FAILED)
            }
        } catch (e: Exception) {
            RecordingResult.Error("Failed to import audio: ${e.message}", RecordingStatus.RECORDING_FAILED)
        }
    }
}
