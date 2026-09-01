package com.example.data.local

import androidx.room.*
import com.example.model.CallRecord
import com.example.model.RecordingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY startedAt DESC")
    fun getAllCalls(): Flow<List<CallRecord>>

    @Query("SELECT * FROM calls WHERE callId = :id")
    suspend fun getCallById(id: String): CallRecord?

    @Query("SELECT * FROM calls WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED' ORDER BY startedAt ASC")
    suspend fun getPendingSyncCalls(): List<CallRecord>

    @Query("SELECT * FROM calls WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCallsFlow(): Flow<List<CallRecord>>

    @Query("SELECT * FROM calls WHERE leadId = :leadId ORDER BY startedAt DESC")
    fun getCallsForLead(leadId: String): Flow<List<CallRecord>>

    @Query("SELECT * FROM calls WHERE phoneNumber LIKE '%' || :query || '%' OR leadName LIKE '%' || :query || '%' ORDER BY startedAt DESC")
    fun searchCalls(query: String): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallRecord)

    @Update
    suspend fun updateCall(call: CallRecord)

    @Query("UPDATE calls SET syncStatus = :syncStatus WHERE callId = :callId")
    suspend fun updateSyncStatus(callId: String, syncStatus: String)

    @Query("UPDATE calls SET recordingStatus = :status WHERE callId = :callId")
    suspend fun updateRecordingStatus(callId: String, status: RecordingStatus)

    @Query("UPDATE calls SET leadId = :leadId, leadName = :leadName, leadCompany = :leadCompany WHERE callId = :callId")
    suspend fun updateLeadAssociation(callId: String, leadId: String?, leadName: String?, leadCompany: String?)

    @Query("DELETE FROM calls WHERE callId = :id")
    suspend fun deleteCall(id: String)

    @Query("SELECT COUNT(*) FROM calls")
    fun getCallsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE recordingStatus = 'RECORDED_LOCAL' OR recordingStatus = 'UPLOADED'")
    fun getRecordedCallsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE recordingStatus = 'UPLOAD_PENDING'")
    fun getPendingUploadsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE recordingStatus = 'UPLOAD_FAILED' OR recordingStatus = 'RECORDING_FAILED'")
    fun getFailedUploadsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM calls WHERE syncStatus = 'PENDING'")
    fun getUnsyncedCallsCount(): Flow<Int>
}
