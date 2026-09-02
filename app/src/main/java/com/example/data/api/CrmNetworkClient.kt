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
}
