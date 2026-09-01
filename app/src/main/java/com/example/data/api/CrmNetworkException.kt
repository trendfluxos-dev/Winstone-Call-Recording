package com.example.data.api

import java.io.IOException

/**
 * Structured exceptions for Winstone CRM network and sync failures.
 */
sealed class CrmNetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class OfflineException(message: String = "Device is offline. Queued for background CRM sync.") : CrmNetworkException(message)
    class TimeoutException(message: String = "Connection to Winstone CRM timed out.") : CrmNetworkException(message)
    class UnauthorizedException(val code: Int = 401, message: String = "Session expired or invalid credentials.") : CrmNetworkException(message)
    class RateLimitedException(val retryAfterSeconds: Int, message: String = "Rate limited by Winstone CRM. Retrying later.") : CrmNetworkException(message)
    class ServerErrorException(val code: Int, message: String = "Winstone CRM server error ($code).") : CrmNetworkException(message)
    class ApiException(val code: Int, message: String) : CrmNetworkException(message)
}
