package com.example.data.local

import androidx.room.*
import com.example.model.CrmLead
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY name ASC")
    fun getAllLeads(): Flow<List<CrmLead>>

    @Query("SELECT * FROM leads WHERE leadId = :id")
    suspend fun getLeadById(id: String): CrmLead?

    @Query("SELECT * FROM leads WHERE normalizedNumber = :normalizedNumber")
    suspend fun findLeadsByNormalizedNumber(normalizedNumber: String): List<CrmLead>

    @Query("SELECT * FROM leads WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun searchLeads(query: String): Flow<List<CrmLead>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<CrmLead>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: CrmLead)

    @Query("UPDATE leads SET lastContactedAt = :timestamp, callsCount = callsCount + 1 WHERE leadId = :leadId")
    suspend fun recordCallForLead(leadId: String, timestamp: Long)

    @Query("SELECT * FROM leads ORDER BY name ASC")
    suspend fun getAllLeadsList(): List<CrmLead>

    @Query("DELETE FROM leads")
    suspend fun clearAllLeads()

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadsCount(): Flow<Int>
}
