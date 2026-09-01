package com.example.telephony

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.WinstoneApp
import com.example.model.CallDirection
import com.example.model.RecordingMetadata
import com.example.model.RecordingStatus
import com.example.sync.CrmSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Enterprise Foreground Service that utilizes MediaRecorder to capture audio
 * during active phone calls, ensuring the recording process persists even if the app UI is closed.
 */
class CallRecordingForegroundService : Service() {

    private val TAG = "CallRecordingService"
    private val CHANNEL_ID = "winstone_active_call_channel"
    private val NOTIFICATION_ID = 2002

    private var mediaRecorder: MediaRecorder? = null
    private var activeOutputFile: File? = null
    private var activeCallId: String = ""
    private var activePhoneNumber: String = ""
    private var activeLeadId: String? = null
    private var activeDirection: CallDirection = CallDirection.OUTGOING
    private var callStartTimeMs: Long = 0L
    private var isRecording: Boolean = false

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var notificationUpdateJob: Job? = null

    companion object {
        const val ACTION_START_CALL = "com.example.winstone.action.START_CALL"
        const val ACTION_STOP_CALL = "com.example.winstone.action.STOP_CALL"
        const val ACTION_PHONE_STATE = "com.example.winstone.action.PHONE_STATE"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_LEAD_ID = "extra_lead_id"
        const val EXTRA_DIRECTION = "extra_direction"
        const val EXTRA_STATE = "extra_state"

        fun startRecording(context: Context, phoneNumber: String, direction: CallDirection, leadId: String? = null) {
            val intent = Intent(context, CallRecordingForegroundService::class.java).apply {
                action = ACTION_START_CALL
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(EXTRA_DIRECTION, direction.name)
                putExtra(EXTRA_LEAD_ID, leadId)
                putExtra(EXTRA_CALL_ID, "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, CallRecordingForegroundService::class.java).apply {
                action = ACTION_STOP_CALL
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i(TAG, "CallRecordingForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action ?: ""
        Log.d(TAG, "onStartCommand received action: $action")

        when (action) {
            ACTION_START_CALL -> {
                val number = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: "Active Call"
                val dirName = intent.getStringExtra(EXTRA_DIRECTION) ?: CallDirection.OUTGOING.name
                val dir = try { CallDirection.valueOf(dirName) } catch (_: Exception) { CallDirection.OUTGOING }
                val leadId = intent.getStringExtra(EXTRA_LEAD_ID)
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: "call_${System.currentTimeMillis()}"

                startRecordingSession(callId, number, dir, leadId)
            }

            ACTION_STOP_CALL -> {
                stopRecordingSession()
            }

            ACTION_PHONE_STATE -> {
                val state = intent.getStringExtra(EXTRA_STATE) ?: ""
                val number = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
                handlePhoneStateTransition(state, number)
            }
        }

        return START_STICKY
    }

    private fun handlePhoneStateTransition(state: String, number: String) {
        Log.d(TAG, "Phone state transition: state=$state, number=$number")
        if (number.isNotBlank()) {
            activePhoneNumber = number
        }

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                activeDirection = CallDirection.INCOMING
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (!isRecording) {
                    val callId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                    startRecordingSession(callId, activePhoneNumber.ifBlank { "Incoming Call" }, activeDirection, null)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (isRecording) {
                    stopRecordingSession()
                }
            }
        }
    }

    private fun startRecordingSession(
        callId: String,
        phoneNumber: String,
        direction: CallDirection,
        leadId: String?
    ) {
        activeCallId = callId
        activePhoneNumber = phoneNumber
        activeDirection = direction
        activeLeadId = leadId
        callStartTimeMs = System.currentTimeMillis()

        // 1. Post Foreground Notification immediately
        val initialNotification = buildRecordingNotification("Recording in progress...", phoneNumber, 0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not startForeground with specified types, falling back", e)
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        // 2. Initialize MediaRecorder
        try {
            val privateDir = File(filesDir, "winstone_recordings").apply {
                if (!exists()) mkdirs()
            }
            val recordingFile = File(privateDir, "rec_${callId}_${System.currentTimeMillis()}.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(recordingFile.absolutePath)

            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            activeOutputFile = recordingFile
            isRecording = true
            Log.i(TAG, "MediaRecorder started successfully on file: ${recordingFile.absolutePath}")

            // 3. Start Notification ticker updates
            startNotificationUpdater()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaRecorder: ${e.message}", e)
            isRecording = false
            mediaRecorder = null
        }
    }

    private fun startNotificationUpdater() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            while (isActive && isRecording) {
                val elapsedSeconds = ((System.currentTimeMillis() - callStartTimeMs) / 1000).toInt()
                val updatedNotification = buildRecordingNotification(
                    "Recording active (${formatSeconds(elapsedSeconds)})",
                    activePhoneNumber,
                    elapsedSeconds
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, updatedNotification)
                delay(1000)
            }
        }
    }

    private fun stopRecordingSession() {
        Log.i(TAG, "Stopping recording session for call $activeCallId")
        notificationUpdateJob?.cancel()
        isRecording = false

        val endTimeMs = System.currentTimeMillis()
        val durationSeconds = ((endTimeMs - callStartTimeMs) / 1000).coerceAtLeast(1)
        val file = activeOutputFile
        val callId = activeCallId.ifBlank { "call_${System.currentTimeMillis()}" }
        val phoneNumber = activePhoneNumber.ifBlank { "Unknown" }
        val leadId = activeLeadId

        var recordingStatus = RecordingStatus.NOT_RECORDED
        var savedRecordingMetadata: RecordingMetadata? = null

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            if (file != null && file.exists() && file.length() > 0) {
                recordingStatus = RecordingStatus.RECORDED_LOCAL
                savedRecordingMetadata = RecordingMetadata(
                    recordingId = UUID.randomUUID().toString(),
                    callId = callId,
                    leadId = leadId,
                    filePath = file.absolutePath,
                    mimeType = "audio/mp4",
                    fileSize = file.length(),
                    durationMs = durationSeconds * 1000,
                    createdAt = System.currentTimeMillis(),
                    uploadStatus = RecordingStatus.RECORDED_LOCAL
                )
                Log.i(TAG, "Audio recorded successfully. Size: ${file.length()} bytes, duration: ${durationSeconds}s")
            } else {
                recordingStatus = RecordingStatus.RECORDING_FAILED
                Log.w(TAG, "Recording file empty or missing after stop")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaRecorder: ${e.message}", e)
            recordingStatus = RecordingStatus.RECORDING_FAILED
            try { mediaRecorder?.release() } catch (_: Exception) {}
            mediaRecorder = null
        }

        // Persist call record & recording metadata in Room, then trigger WorkManager sync
        serviceScope.launch {
            val app = applicationContext as? WinstoneApp ?: return@launch
            val repository = app.repository

            if (savedRecordingMetadata != null) {
                repository.saveRecording(savedRecordingMetadata)
            }

            repository.logCallActivity(
                phoneNumber = phoneNumber,
                direction = activeDirection,
                startedAt = callStartTimeMs,
                endedAt = endTimeMs,
                durationSeconds = durationSeconds,
                recordingStatus = recordingStatus
            )

            // Trigger WorkManager task to sync pending calls and recordings when network is connected
            CrmSyncWorker.enqueueImmediateSync(applicationContext)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildRecordingNotification(statusText: String, number: String, seconds: Int): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, CallRecordingForegroundService::class.java).apply {
            action = ACTION_STOP_CALL
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Winstone Call Recorder")
            .setContentText("$number • $statusText")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop & Sync", stopPendingIntent)
            .build()
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d", m, s)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Call Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing call recording status and duration"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notificationUpdateJob?.cancel()
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        Log.i(TAG, "CallRecordingForegroundService destroyed")
    }
}
