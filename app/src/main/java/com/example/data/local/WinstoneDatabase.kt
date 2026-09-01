package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.*

@Database(
    entities = [
        CallRecord::class,
        RecordingMetadata::class,
        CrmLead::class,
        DeviceProfile::class,
        AuditLogEntry::class,
        SyncQueueItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WinstoneDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun recordingDao(): RecordingDao
    abstract fun leadDao(): LeadDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun deviceProfileDao(): DeviceProfileDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: WinstoneDatabase? = null

        fun getDatabase(context: Context): WinstoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WinstoneDatabase::class.java,
                    "winstone_crm_calls.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
