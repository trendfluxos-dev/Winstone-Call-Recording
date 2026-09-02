package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.WinstoneApp
import com.example.model.CallDirection
import com.example.model.CallRecord
import com.example.model.ConsentStatus
import com.example.model.RecordingStatus
import com.example.sync.CrmSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * BroadcastReceiver for 'android.intent.action.PHONE_STATE' and 'android.intent.action.NEW_OUTGOING_CALL'.
 * Captures call lifecycle events (Ringing, Off-hook, Idle), normalizes phone numbers,
 * stores call metadata into the local Room database, and triggers WorkManager for secure CRM sync.
 */
class CallReceiver : BroadcastReceiver() {

    private val TAG = "CallReceiver"

    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var callStartTimeMs = 0L
        private var callAnsweredTimeMs: Long? = null
        private var currentPhoneNumber = ""
        private var currentDirection = CallDirection.INCOMING
        private var currentCallId = ""
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action=$action")

        if (action == Intent.ACTION_NEW_OUTGOING_CALL) {
            val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            currentPhoneNumber = outgoingNumber
            currentDirection = CallDirection.OUTGOING
            currentCallId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            callStartTimeMs = System.currentTimeMillis()
            callAnsweredTimeMs = null
            Log.d(TAG, "Outgoing call initiated to: $outgoingNumber (CallID: $currentCallId)")

            forwardToServices(context, "OUTGOING_CALL", TelephonyManager.EXTRA_STATE_OFFHOOK, outgoingNumber, CallDirection.OUTGOING)
            return
        }

        if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""

            if (incomingNumber.isNotBlank()) {
                currentPhoneNumber = incomingNumber
            }

            Log.d(TAG, "Phone State changed from $lastState to $stateStr for number: $currentPhoneNumber")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call started ringing
                    currentDirection = CallDirection.INCOMING
                    currentCallId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                    callStartTimeMs = System.currentTimeMillis()
                    callAnsweredTimeMs = null
                    Log.i(TAG, "Incoming call ringing: $currentPhoneNumber (CallID: $currentCallId)")
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call answered / active
                    if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                        currentDirection = CallDirection.INCOMING
                    } else if (lastState == TelephonyManager.EXTRA_STATE_IDLE) {
                        currentDirection = CallDirection.OUTGOING
                        if (currentCallId.isBlank()) {
                            currentCallId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                            callStartTimeMs = System.currentTimeMillis()
                        }
                    }
                    callAnsweredTimeMs = System.currentTimeMillis()
                    Log.i(TAG, "Call off-hook (active) with: $currentPhoneNumber ($currentDirection)")
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended or missed
                    if (lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                        val endTimeMs = System.currentTimeMillis()
                        val durationSec = if (callAnsweredTimeMs != null) {
                            ((endTimeMs - callAnsweredTimeMs!!) / 1000).coerceAtLeast(1)
                        } else if (callStartTimeMs > 0L) {
                            ((endTimeMs - callStartTimeMs) / 1000).coerceAtLeast(0)
                        } else {
                            0L
                        }

                        Log.i(TAG, "Call ended with: $currentPhoneNumber, duration: ${durationSec}s. Storing to Room & scheduling sync...")

                        storeCallToRoomAndSync(
                            context = context,
                            callId = if (currentCallId.isNotBlank()) currentCallId else "call_${System.currentTimeMillis()}",
                            phoneNumber = currentPhoneNumber.ifBlank { "Unknown" },
                            direction = currentDirection,
                            startedAt = if (callStartTimeMs > 0L) callStartTimeMs else endTimeMs - (durationSec * 1000),
                            answeredAt = callAnsweredTimeMs,
                            endedAt = endTimeMs,
                            durationSeconds = durationSec
                        )

                        // Reset state
                        currentCallId = ""
                        callStartTimeMs = 0L
                        callAnsweredTimeMs = null
                        currentPhoneNumber = ""
                    }
                }
            }

            lastState = stateStr
            forwardToServices(context, "STATE_CHANGED", stateStr, currentPhoneNumber, currentDirection)
        }
    }

    private fun storeCallToRoomAndSync(
        context: Context,
        callId: String,
        phoneNumber: String,
        direction: CallDirection,
        startedAt: Long,
        answeredAt: Long?,
        endedAt: Long,
        durationSeconds: Long
    ) {
        val appContext = context.applicationContext as? WinstoneApp ?: return
        val normalized = PhoneNumberUtils.normalize(phoneNumber)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = appContext.database
                val callDao = db.callDao()
                val leadDao = db.leadDao()

                // Look up matching Lead by normalized or raw number
                val matchedLeads = leadDao.findLeadsByNormalizedNumber(normalized)
                val matchedLead = matchedLeads.firstOrNull()

                val agentId = appContext.authRepository.session.value.agentId

                val callRecord = CallRecord(
                    callId = callId,
                    phoneNumber = phoneNumber,
                    normalizedNumber = normalized,
                    direction = direction,
                    startedAt = startedAt,
                    answeredAt = answeredAt,
                    endedAt = endedAt,
                    durationSeconds = durationSeconds,
                    agentId = agentId,
                    deviceId = appContext.authRepository.getDeviceId(),
                    leadId = matchedLead?.leadId,
                    leadName = matchedLead?.name,
                    leadCompany = matchedLead?.company,
                    outcome = if (durationSeconds > 0) "Completed Call" else "Missed / No Answer",
                    notes = if (matchedLead != null) "Auto-associated with lead: ${matchedLead.name}" else "",
                    recordingStatus = RecordingStatus.NOT_RECORDED,
                    consentStatus = ConsentStatus.INFORMATIONAL,
                    syncStatus = "PENDING",
                    idempotencyKey = "call_${callId}_${UUID.randomUUID().toString().take(8)}"
                )

                callDao.insertCall(callRecord)
                Log.i(TAG, "Call record $callId successfully stored in Room DB. Triggering WorkManager sync...")

                // Secure background synchronization via WorkManager
                CrmSyncWorker.enqueueImmediateSync(appContext)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to store call record into Room: ${e.message}", e)
            }
        }
    }

    private fun forwardToServices(
        context: Context,
        actionName: String,
        stateStr: String,
        number: String,
        direction: CallDirection
    ) {
        try {
            val serviceIntent = Intent(context, CallTrackingService::class.java).apply {
                putExtra("action", actionName)
                putExtra("state", stateStr)
                putExtra("number", number)
            }
            context.startService(serviceIntent)

            val recordingIntent = Intent(context, CallRecordingForegroundService::class.java).apply {
                if (actionName == "OUTGOING_CALL") {
                    setAction(CallRecordingForegroundService.ACTION_START_CALL)
                    putExtra(CallRecordingForegroundService.EXTRA_PHONE_NUMBER, number)
                    putExtra(CallRecordingForegroundService.EXTRA_DIRECTION, direction.name)
                } else {
                    setAction(CallRecordingForegroundService.ACTION_PHONE_STATE)
                    putExtra(CallRecordingForegroundService.EXTRA_STATE, stateStr)
                    putExtra(CallRecordingForegroundService.EXTRA_PHONE_NUMBER, number)
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(recordingIntent)
            } else {
                context.startService(recordingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not forward call event to foreground services: ${e.message}")
        }
    }
}
