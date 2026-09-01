package com.example.telephony

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.WinstoneApp
import com.example.model.CallDirection
import com.example.model.RecordingCapability
import com.example.model.RecordingStatus
import com.example.recording.NativeDeviceRecordingProvider
import com.example.recording.RecordingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallTrackingService : Service() {

    private val TAG = "CallTrackingService"
    private val CHANNEL_ID = "winstone_call_tracking_channel"
    private val NOTIFICATION_ID = 1001

    private var callStartTime: Long = 0L
    private var activePhoneNumber: String = ""
    private var currentDirection: CallDirection = CallDirection.OUTGOING
    private var isCallActive = false
    private var recordingProvider: NativeDeviceRecordingProvider? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        recordingProvider = NativeDeviceRecordingProvider(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.getStringExtra("action") ?: ""
        val state = intent.getStringExtra("state") ?: ""
        val number = intent.getStringExtra("number") ?: ""

        if (number.isNotBlank()) {
            activePhoneNumber = number
        }

        Log.d(TAG, "onStartCommand: action=$action, state=$state, number=$number")

        when {
            action == "OUTGOING_CALL" -> {
                currentDirection = CallDirection.OUTGOING
                handleCallStarted(number)
            }
            state == TelephonyManager.EXTRA_STATE_RINGING -> {
                currentDirection = CallDirection.INCOMING
                Log.d(TAG, "Incoming call ringing: $number")
            }
            state == TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (!isCallActive) {
                    handleCallStarted(activePhoneNumber)
                }
            }
            state == TelephonyManager.EXTRA_STATE_IDLE -> {
                if (isCallActive) {
                    handleCallEnded()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun handleCallStarted(number: String) {
        isCallActive = true
        callStartTime = System.currentTimeMillis()
        val displayNum = if (number.isNotBlank()) number else "Active Call"

        val notification = buildNotification(
            "Winstone Call Monitor",
            "Call in progress with $displayNum"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Check capability
        val repository = (application as WinstoneApp).repository
        val capability = repository.checkDeviceCapability()

        if (capability.status == RecordingCapability.SUPPORTED && repository.policy.value.autoRecordingEnabled) {
            // Attempt permitted native recording
            recordingProvider?.startRecording("call_${System.currentTimeMillis()}", null)
        }
    }

    private fun handleCallEnded() {
        isCallActive = false
        val endTime = System.currentTimeMillis()
        val durationSeconds = ((endTime - callStartTime) / 1000).coerceAtLeast(1)
        val phone = if (activePhoneNumber.isNotBlank()) activePhoneNumber else "Unknown Number"

        val repository = (application as WinstoneApp).repository
        val capability = repository.checkDeviceCapability()

        // Stop recording if one was running
        var recStatus = if (capability.status == RecordingCapability.UNAVAILABLE) {
            RecordingStatus.RECORDING_UNAVAILABLE
        } else {
            RecordingStatus.NOT_RECORDED
        }

        val recResult = recordingProvider?.stopRecording()
        if (recResult is RecordingResult.Success) {
            recStatus = RecordingStatus.RECORDED_LOCAL
            CoroutineScope(Dispatchers.IO).launch {
                repository.saveRecording(recResult.metadata)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            repository.logCallActivity(
                phoneNumber = phone,
                direction = currentDirection,
                startedAt = callStartTime,
                endedAt = endTime,
                durationSeconds = durationSeconds,
                recordingStatus = recStatus
            )
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(title: String, text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Winstone Call Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground call tracking and CRM synchronization"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
