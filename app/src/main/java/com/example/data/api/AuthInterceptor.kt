package com.example.data.api

import com.example.data.repository.AuthRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor that injects Winstone CRM authentication token and custom enterprise headers.
 */
class AuthInterceptor(private val authRepository: AuthRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Attach Bearer Token if present
        val token = authRepository.getAuthToken()
        if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // Standard Winstone Enterprise CRM Headers
        requestBuilder.header("Accept", "application/json")
        requestBuilder.header("X-App-Version", "1.0.0")
        requestBuilder.header("X-Client-Platform", "Android-16")
        requestBuilder.header("X-Device-Model", "Proton HyperX")
        requestBuilder.header("X-Device-Id", authRepository.getDeviceId())
        
        val currentSession = authRepository.session.value
        if (currentSession.agentId.isNotBlank()) {
            requestBuilder.header("X-Agent-Id", currentSession.agentId)
            requestBuilder.header("X-Employee-Id", currentSession.employeeId)
        }

        // Support dynamic CRM host resolution (e.g. https://crm.winstonebd.com/)
        val currentBaseUrl = authRepository.getBaseUrl()
        val currentHttpUrl = currentBaseUrl.toHttpUrlOrNull()
        if (currentHttpUrl != null) {
            val originalUrl = originalRequest.url
            if (originalUrl.host != currentHttpUrl.host) {
                val newUrl = originalUrl.newBuilder()
                    .scheme(currentHttpUrl.scheme)
                    .host(currentHttpUrl.host)
                    .port(currentHttpUrl.port)
                    .build()
                requestBuilder.url(newUrl)
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
