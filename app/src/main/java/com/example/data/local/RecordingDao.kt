package com.example.data.local

import androidx.room.*
import com.example.model.RecordingMetadata
import com.example.model.RecordingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<RecordingMetadata>>

    @Query("SELECT * FROM recordings WHERE callId = :callId LIMIT 1")
    suspend fun getRecordingForCall(callId: String): RecordingMetadata?

    @Query("SELECT * FROM recordings WHERE recordingId = :id")
    suspend fun getRecordingById(id: String): RecordingMetadata?

    @Query("SELECT * FROM recordings WHERE uploadStatus = 'UPLOAD_PENDING' OR uploadStatus = 'RECORDED_LOCAL' OR uploadStatus = 'UPLOAD_FAILED'")
    suspend fun getPendingUploadRecordings(): List<RecordingMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingMetadata)

    @Update
    suspend fun updateRecording(recording: RecordingMetadata)

    @Query("UPDATE recordings SET uploadStatus = :status, lastError = :error WHERE recordingId = :id")
    suspend fun updateUploadStatus(id: String, status: RecordingStatus, error: String? = null)

    @Query("DELETE FROM recordings WHERE recordingId = :id")
    suspend fun deleteRecording(id: String)
}
