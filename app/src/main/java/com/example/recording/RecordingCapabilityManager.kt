package com.example.recording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.model.RecordingCapability
import java.io.File

/**
 * Evaluates legitimate cellular and voice recording capability on the host device.
 * Strictly adheres to Android 16 platform restrictions and never attempts bypasses,
 * unauthorized accessibility hacks, or covert audio tapping.
 */
class RecordingCapabilityManager(private val context: Context) {

    /**
     * Checks if the current Android environment and hardware profile permit
     * direct automated call audio recording.
     */
    fun checkRecordingCapability(): CapabilityCheckResult {
        // Step 1: Check permissions
        val hasRecordAudio = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasReadPhoneState = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasRecordAudio || !hasReadPhoneState) {
            return CapabilityCheckResult(
                status = RecordingCapability.UNAVAILABLE,
                titleEn = "Permissions Missing",
                titleBn = "প্রয়োজনীয় পারমিশন দেওয়া হয়নি",
                detailsEn = "Record Audio and Phone State permissions are required for call logging and testing.",
                detailsBn = "কল রেকর্ডিং ও সনাক্তকরণের জন্য অডিও ও ফোন স্টেট পারমিশন প্রয়োজন।",
                canRecordMic = false,
                canRecordCallStream = false
            )
        }

        // Step 2: Evaluate Android 16 / OEM Cellular call audio policy
        // On modern Android (API 34, 35, 36/Android 16), third-party non-system dialer apps
        // are prevented from capturing the remote party cellular audio stream (VOICE_CALL / VOICE_DOWNLINK)
        // by the Android Audio HAL security sandbox.
        val isModernAndroid = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        val isHyperXDevice = Build.MODEL.contains("HyperX", ignoreCase = true) ||
                Build.DEVICE.contains("HyperX", ignoreCase = true) ||
                Build.PRODUCT.contains("HyperX", ignoreCase = true)

        // Test if standard MIC input is operational for manual recording / voice notes
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val hasMicrophoneDevice = audioManager?.getDevices(AudioManager.GET_DEVICES_INPUTS)?.isNotEmpty() == true

        return if (isModernAndroid) {
            // Android 16 platform restriction: Direct two-sided cellular call audio is restricted by OS
            CapabilityCheckResult(
                status = RecordingCapability.UNAVAILABLE,
                titleEn = "Automatic call recording unavailable",
                titleBn = "এই ডিভাইসে স্বয়ংক্রিয় কল রেকর্ডিং উপলভ্য নয়",
                detailsEn = "Android 16 voice communication security policy prevents third-party apps from capturing remote cellular audio. Call metadata logging and manual authorized uploads are active.",
                detailsBn = "অ্যান্ড্রয়েড ১৬ সিকিউরিটি পলিসির কারণে টু-ওয়ে সেলুলার কল অডিও সীমাবদ্ধ। কল মেটাডাটা লগিং ও ম্যানুয়াল আপলোড সচল রয়েছে।",
                canRecordMic = hasMicrophoneDevice,
                canRecordCallStream = false
            )
        } else {
            CapabilityCheckResult(
                status = RecordingCapability.LIMITED,
                titleEn = "Limited Capability",
                titleBn = "সীমিত রেকর্ডিং সুবিধা",
                detailsEn = "Device supports ambient microphone recording, but carrier/OEM restrictions may limit remote audio clarity.",
                detailsBn = "ডিভাইসে মাইক্রোফোন রেকর্ডিং সচল, তবে ক্যারিয়ার বা প্রস্তুতকারকের সীমাবদ্ধতা থাকতে পারে।",
                canRecordMic = true,
                canRecordCallStream = false
            )
        }
    }
}

data class CapabilityCheckResult(
    val status: RecordingCapability,
    val titleEn: String,
    val titleBn: String,
    val detailsEn: String,
    val detailsBn: String,
    val canRecordMic: Boolean,
    val canRecordCallStream: Boolean
)
