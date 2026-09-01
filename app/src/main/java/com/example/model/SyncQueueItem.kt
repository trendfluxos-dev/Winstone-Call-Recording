package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Offline Sync Queue Item for reliable retry and synchronization with https://crm.winstonebd.com/
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["nextRetryAt"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class SyncQueueItem(
    @PrimaryKey val queueId: String = UUID.randomUUID().toString(),
    val itemType: SyncItemType, // CALL_LOG, RECORDING_UPLOAD, LEAD_NOTE
    val entityId: String,
    val payloadJson: String,
    val status: SyncQueueStatus = SyncQueueStatus.PENDING,
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val nextRetryAt: Long = System.currentTimeMillis(),
    val idempotencyKey: String = UUID.randomUUID().toString()
)

enum class SyncItemType {
    CALL_LOG,
    RECORDING_UPLOAD,
    LEAD_NOTE,
    DEVICE_HEARTBEAT
}

enum class SyncQueueStatus {
    PENDING,
    IN_PROGRESS,
    FAILED,
    COMPLETED
}
