package com.example.data.api

import android.util.Log
import com.example.data.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Interceptor that intercepts CRM API network errors and HTTP error codes,
 * converting them into structured CrmNetworkExceptions and triggering offline sync fallback.
 */
class SyncErrorInterceptor(private val authRepository: AuthRepository) : Interceptor {

    private val TAG = "SyncErrorInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response: Response = try {
            chain.proceed(request)
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Host unreachable for ${request.url}: ${e.message}")
            throw CrmNetworkException.OfflineException("Cannot reach https://crm.winstonebd.com/ - device is offline or DNS failed.")
        } catch (e: ConnectException) {
            Log.w(TAG, "Connection refused for ${request.url}: ${e.message}")
            throw CrmNetworkException.OfflineException("Connection failed to Winstone CRM server.")
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Socket timeout for ${request.url}")
            throw CrmNetworkException.TimeoutException("CRM server request timed out. Queued for retry.")
        } catch (e: IOException) {
            Log.e(TAG, "I/O error during request: ${e.message}", e)
            throw CrmNetworkException.OfflineException("Network I/O failure: ${e.localizedMessage}")
        }

        // Handle HTTP Error Scenarios
        when (response.code) {
            401 -> {
                Log.w(TAG, "HTTP 401 Unauthorized received for ${request.url}")
                authRepository.onSessionExpired()
                // Let the response pass through or throw exception so caller knows
            }
            429 -> {
                val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 60
                Log.w(TAG, "HTTP 429 Rate limited. Retry-After: $retryAfter seconds")
                throw CrmNetworkException.RateLimitedException(retryAfter)
            }
            500, 502, 503, 504 -> {
                Log.w(TAG, "HTTP ${response.code} Server Error on Winstone CRM backend for ${request.url} - keeping request in offline sync queue")
                // Do not crash the app, provide graceful offline fallback
            }
        }

        return response
    }
}
