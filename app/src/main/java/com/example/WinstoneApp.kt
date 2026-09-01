package com.example

import android.app.Application
import com.example.data.api.CrmNetworkClient
import com.example.data.local.WinstoneDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.WinstoneRepository

class WinstoneApp : Application() {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var database: WinstoneDatabase
        private set

    lateinit var networkClient: CrmNetworkClient
        private set

    lateinit var repository: WinstoneRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = AuthRepository(this)
        networkClient = CrmNetworkClient(this, authRepository)
        database = WinstoneDatabase.getDatabase(this)
        repository = WinstoneRepository(this, database, networkClient, authRepository)
        
        // Enqueue background WorkManager task to automatically sync recordings and calls on network connect
        com.example.sync.CrmSyncWorker.enqueuePeriodicSync(this)
    }
}
