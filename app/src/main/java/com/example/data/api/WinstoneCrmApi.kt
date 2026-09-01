package com.example.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for Winstone CRM APIs (https://crm.winstonebd.com/).
 */
interface WinstoneCrmApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<LoginResponse>

    @GET("api/v1/auth/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<LoginResponse>

    @POST("api/v1/devices/register")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body request: DeviceRegisterRequest
    ): Response<DeviceRegisterResponse>

    @POST("api/v1/calls/log")
    suspend fun logCall(
        @Header("Authorization") token: String,
        @Body payload: CallSyncPayload
    ): Response<CallSyncResponse>

    @POST("api/v1/calls/sync")
    suspend fun syncBatchCalls(
        @Header("Authorization") token: String,
        @Body calls: List<CallSyncPayload>
    ): Response<List<CallSyncResponse>>

    @Multipart
    @POST("api/v1/recordings/upload")
    suspend fun uploadRecording(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("call_id") callId: RequestBody,
        @Part("lead_id") leadId: RequestBody?,
        @Part("duration_seconds") durationSeconds: RequestBody,
        @Part("consent_status") consentStatus: RequestBody,
        @Part("idempotency_key") idempotencyKey: RequestBody
    ): Response<RecordingUploadResponse>

    @GET("api/v1/leads")
    suspend fun getAssignedLeads(
        @Header("Authorization") token: String
    ): Response<LeadsResponse>

    @GET("api/v1/leads/search")
    suspend fun searchLeads(
        @Header("Authorization") token: String,
        @Query("phone") phone: String? = null,
        @Query("q") query: String? = null
    ): Response<LeadsResponse>

    @GET("api/v1/leads/match")
    suspend fun matchLeadByPhone(
        @Header("Authorization") token: String,
        @Query("phone") normalizedPhone: String
    ): Response<LeadsResponse>
}
