package com.example.data.api

import com.example.model.CrmLead
import com.example.model.RecordingStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
    val employeeId: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val accessToken: String,
    val agentId: String,
    val fullName: String,
    val role: String,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    val deviceId: String,
    val model: String,
    val androidVersion: String,
    val buildNumber: String,
    val appVersion: String,
    val capabilityStatus: String
)

@JsonClass(generateAdapter = true)
data class DeviceRegisterResponse(
    val success: Boolean,
    val registered: Boolean,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CallSyncPayload(
    val callId: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val direction: String,
    val startedAt: Long,
    val durationSeconds: Long,
    val leadId: String?,
    val outcome: String?,
    val notes: String?,
    val followUpDate: String?,
    val recordingStatus: String,
    val idempotencyKey: String
)

@JsonClass(generateAdapter = true)
data class CallSyncResponse(
    val success: Boolean,
    val callId: String,
    val leadMatched: Boolean,
    val leadId: String? = null
)

@JsonClass(generateAdapter = true)
data class RecordingUploadResponse(
    val success: Boolean,
    val recordingId: String,
    val storageUrl: String? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class LeadsResponse(
    val success: Boolean,
    val leads: List<CrmLead>
)
