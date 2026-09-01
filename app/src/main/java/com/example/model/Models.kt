package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Recording capability states according to Android 16 / device environment.
 */
enum class RecordingCapability {
    SUPPORTED,
    LIMITED,
    UNAVAILABLE,
    UNKNOWN
}

/**
 * Exact recording statuses as required by Winstone CRM specification.
 */
enum class RecordingStatus(val labelEn: String, val labelBn: String) {
    NOT_RECORDED("Not Recorded", "রেকর্ড করা হয়নি"),
    RECORDING("Recording...", "রেকর্ডিং চলছে..."),
    RECORDED_LOCAL("Recorded Locally", "লোকালি সংরক্ষিত"),
    UPLOAD_PENDING("Upload Pending", "আপলোড অপেক্ষমাণ"),
    UPLOADING("Uploading...", "আপলোড হচ্ছে..."),
    UPLOADED("Uploaded to CRM", "CRM-এ আপলোড সম্পন্ন"),
    UPLOAD_FAILED("Upload Failed", "আপলোড ব্যর্থ"),
    RECORDING_UNAVAILABLE("Recording Unavailable", "রেকর্ডিং উপলভ্য নয়"),
    RECORDING_FAILED("Recording Failed", "রেকর্ডিং ব্যর্থ"),
    DELETED("Deleted", "মুছে ফেলা হয়েছে")
}

/**
 * Call direction: Incoming or Outgoing.
 */
enum class CallDirection {
    INCOMING,
    OUTGOING
}

/**
 * Consent mode for call recording disclosure.
 */
enum class ConsentStatus {
    PENDING,
    OBTAINED,
    DECLINED,
    INFORMATIONAL,
    UNKNOWN
}

/**
 * User roles in Winstone CRM.
 */
enum class UserRole {
    AGENT,
    AUTHORITY
}

/**
 * Room Entity representing a logged phone call.
 */
@Entity(tableName = "calls")
data class CallRecord(
    @PrimaryKey val callId: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val normalizedNumber: String,
    val direction: CallDirection,
    val startedAt: Long = System.currentTimeMillis(),
    val answeredAt: Long? = null,
    val endedAt: Long? = null,
    val durationSeconds: Long = 0,
    val agentId: String = "",
    val deviceId: String = "",
    val leadId: String? = null,
    val leadName: String? = null,
    val leadCompany: String? = null,
    val outcome: String = "No Outcome Set",
    val notes: String = "",
    val followUpDate: String? = null,
    val recordingStatus: RecordingStatus = RecordingStatus.NOT_RECORDED,
    val consentStatus: ConsentStatus = ConsentStatus.UNKNOWN,
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val idempotencyKey: String = UUID.randomUUID().toString()
)

/**
 * Room Entity representing an audio recording file and upload metadata.
 */
@Entity(tableName = "recordings")
data class RecordingMetadata(
    @PrimaryKey val recordingId: String = UUID.randomUUID().toString(),
    val callId: String,
    val leadId: String? = null,
    val filePath: String,
    val mimeType: String = "audio/mp4",
    val fileSize: Long = 0L,
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val uploadStatus: RecordingStatus = RecordingStatus.RECORDED_LOCAL,
    val uploadAttempts: Int = 0,
    val lastError: String? = null,
    val idempotencyKey: String = UUID.randomUUID().toString()
)

/**
 * Room Entity representing a Winstone CRM Lead cached for offline matching.
 */
@Entity(tableName = "leads")
data class CrmLead(
    @PrimaryKey val leadId: String,
    val name: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val email: String,
    val company: String,
    val status: String = "New Lead",
    val assignedAgentId: String,
    val lastContactedAt: Long? = null,
    val notesCount: Int = 0,
    val callsCount: Int = 0
)

/**
 * Room Entity representing Device Registration with CRM.
 */
@Entity(tableName = "device_profile")
data class DeviceProfile(
    @PrimaryKey val deviceId: String = "hyperx_device_01",
    val model: String = "Proton HyperX",
    val androidVersion: String = "Android 16",
    val buildNumber: String = "HyperX_HW1_V5_12152025",
    val appVersion: String = "1.0.0 (Build 100)",
    val capabilityStatus: RecordingCapability = RecordingCapability.UNAVAILABLE,
    val capabilityDetails: String = "Android 16 voice communication security policy detected.",
    val isRegistered: Boolean = true,
    val registeredAgentId: String = "agent_rahim_01",
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

/**
 * Room Entity for Audit Logs.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntry(
    @PrimaryKey val auditId: String = UUID.randomUUID().toString(),
    val action: String, // CALL_CREATED, RECORDING_UPLOADED, etc.
    val actorId: String,
    val role: String,
    val entityId: String,
    val leadId: String? = null,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadataJson: String = "{}"
)

/**
 * Winstone Recording Policy.
 */
data class RecordingPolicy(
    val recordingEnabled: Boolean = true,
    val autoRecordingEnabled: Boolean = true,
    val manualUploadEnabled: Boolean = true,
    val wifiOnlyUpload: Boolean = false,
    val consentMode: String = "INFORMATIONAL", // PENDING, OBTAINED, DECLINED, INFORMATIONAL
    val announcementEnabled: Boolean = true,
    val retentionDays: Int = 90,
    val audioFormat: String = "M4A / AAC",
    val maxFileSizeMb: Int = 25
)

/**
 * User Session object.
 */
data class UserSession(
    val agentId: String = "agent_rahim_01",
    val workEmail: String = "rahim.khan@winstonecrm.com",
    val employeeId: String = "WN-88042",
    val fullName: String = "Rahim Khan",
    val role: UserRole = UserRole.AGENT,
    val accessToken: String = "wn_sec_token_94719284",
    val isLoggedIn: Boolean = true
)

/**
 * Test Call 10-point checklist item.
 */
data class TestCallStep(
    val stepNumber: Int,
    val titleEn: String,
    val titleBn: String,
    val detailsEn: String,
    val detailsBn: String,
    val passed: Boolean,
    val output: String = ""
)

/**
 * Overall Test Call Evaluation.
 */
enum class TestCallVerdict {
    PASS,
    PARTIAL,
    FAILED,
    NOT_RUN
}
