package com.example.data.local

import androidx.room.*
import com.example.model.AuditLogEntry
import com.example.model.DeviceProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(entry: AuditLogEntry)
}

@Dao
interface DeviceProfileDao {
    @Query("SELECT * FROM device_profile WHERE deviceId = :id LIMIT 1")
    fun getDeviceProfile(id: String = "hyperx_device_01"): Flow<DeviceProfile?>

    @Query("SELECT * FROM device_profile WHERE deviceId = :id LIMIT 1")
    suspend fun getDeviceProfileDirect(id: String = "hyperx_device_01"): DeviceProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceProfile(profile: DeviceProfile)
}
