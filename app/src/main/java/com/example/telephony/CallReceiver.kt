package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("CallReceiver", "Received broadcast action: $action")

        if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            val serviceIntent = Intent(context, CallTrackingService::class.java).apply {
                putExtra("action", "STATE_CHANGED")
                putExtra("state", stateStr)
                putExtra("number", incomingNumber)
            }
            val recordingIntent = Intent(context, CallRecordingForegroundService::class.java).apply {
                setAction(CallRecordingForegroundService.ACTION_PHONE_STATE)
                putExtra(CallRecordingForegroundService.EXTRA_STATE, stateStr)
                putExtra(CallRecordingForegroundService.EXTRA_PHONE_NUMBER, incomingNumber)
            }
            try {
                context.startService(serviceIntent)
                context.startService(recordingIntent)
            } catch (e: Exception) {
                Log.e("CallReceiver", "Could not forward to services", e)
            }
        } else if (action == Intent.ACTION_NEW_OUTGOING_CALL) {
            val outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            val serviceIntent = Intent(context, CallTrackingService::class.java).apply {
                putExtra("action", "OUTGOING_CALL")
                putExtra("number", outgoingNumber)
            }
            val recordingIntent = Intent(context, CallRecordingForegroundService::class.java).apply {
                setAction(CallRecordingForegroundService.ACTION_START_CALL)
                putExtra(CallRecordingForegroundService.EXTRA_PHONE_NUMBER, outgoingNumber)
                putExtra(CallRecordingForegroundService.EXTRA_DIRECTION, "OUTGOING")
            }
            try {
                context.startService(serviceIntent)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(recordingIntent)
                } else {
                    context.startService(recordingIntent)
                }
            } catch (e: Exception) {
                Log.e("CallReceiver", "Could not forward outgoing call event", e)
            }
        }
    }
}
