package com.example.data.api

import android.content.Context
import com.example.data.repository.AuthRepository
import com.example.model.CrmLead
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class CrmNetworkClient(
    private val context: Context,
    private val authRepository: AuthRepository
) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Redact authorization tokens & binary audio streams while keeping headers for diagnostics
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val authInterceptor = AuthInterceptor(authRepository)
    private val syncErrorInterceptor = SyncErrorInterceptor(authRepository)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(syncErrorInterceptor)
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AuthRepository.DEFAULT_CRM_BASE_URL) // https://crm.winstonebd.com/
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: WinstoneCrmApi = retrofit.create(WinstoneCrmApi::class.java)

    /**
     * Initial seed leads for Bangladesh CRM sales team (Rahim Khan's assigned territory)
     */
    fun getInitialSeedLeads(): List<CrmLead> {
        return listOf(
            CrmLead(
                leadId = "lead_001",
                name = "Tanvir Ahmed",
                phoneNumber = "01711223344",
                normalizedNumber = "+8801711223344",
                email = "tanvir.ahmed@apex-bd.com",
                company = "Apex Footwear Ltd",
                status = "Qualified / Negotiation",
                assignedAgentId = "agent_rahim_01",
                lastContactedAt = System.currentTimeMillis() - 86400000L * 2,
                notesCount = 4,
                callsCount = 3
            ),
            CrmLead(
                leadId = "lead_002",
                name = "Farzana Yasmin",
                phoneNumber = "01819876543",
                normalizedNumber = "+8801819876543",
                email = "farzana@beximco.net",
                company = "Beximco Pharma",
                status = "Proposal Sent",
                assignedAgentId = "agent_rahim_01",
                lastContactedAt = System.currentTimeMillis() - 86400000L * 1,
                notesCount = 2,
                callsCount = 2
            ),
            CrmLead(
                leadId = "lead_003",
                name = "Mahmudur Rahman",
                phoneNumber = "01912345678",
                normalizedNumber = "+8801912345678",
                email = "m.rahman@squaregroup.com",
                company = "Square Textiles",
                status = "Follow-up Scheduled",
                assignedAgentId = "agent_rahim_01",
                lastContactedAt = System.currentTimeMillis() - 86400000L * 3,
                notesCount = 5,
                callsCount = 4
            ),
            CrmLead(
                leadId = "lead_004",
                name = "Nusrat Jahan",
                phoneNumber = "01678901234",
                normalizedNumber = "+8801678901234",
                email = "nusrat@grameenphone.com",
                company = "Grameen Telecom",
                status = "New Inquiry",
                assignedAgentId = "agent_rahim_01",
                lastContactedAt = null,
                notesCount = 1,
                callsCount = 0
            ),
            CrmLead(
                leadId = "lead_005",
                name = "Kazi Anisur Rahman",
                phoneNumber = "01555667788",
                normalizedNumber = "+8801555667788",
                email = "anis@akijgroup.com",
                company = "Akij Logistics",
                status = "Closed / Won",
                assignedAgentId = "agent_rahim_01",
                lastContactedAt = System.currentTimeMillis() - 86400000L * 5,
                notesCount = 8,
                callsCount = 6
            )
        )
    }
}
