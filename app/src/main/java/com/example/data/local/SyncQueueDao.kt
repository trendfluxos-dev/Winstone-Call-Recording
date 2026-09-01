package com.example.data.local

import androidx.room.*
import com.example.model.SyncItemType
import com.example.model.SyncQueueItem
import com.example.model.SyncQueueStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getAllQueueItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' AND nextRetryAt <= :currentTime ORDER BY createdAt ASC")
    suspend fun getReadyPendingItems(currentTime: Long = System.currentTimeMillis()): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    fun getPendingAndFailedItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueItem)

    @Update
    suspend fun update(item: SyncQueueItem)

    @Query("UPDATE sync_queue SET status = :status, lastError = :error, retryCount = retryCount + 1, nextRetryAt = :nextRetry, updatedAt = :now WHERE queueId = :id")
    suspend fun markFailedWithBackoff(
        id: String,
        status: SyncQueueStatus,
        error: String,
        nextRetry: Long,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE sync_queue SET status = :status, updatedAt = :now WHERE queueId = :id")
    suspend fun updateStatus(id: String, status: SyncQueueStatus, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun purgeCompleted()

    @Query("DELETE FROM sync_queue WHERE queueId = :id")
    suspend fun delete(id: String)
}
