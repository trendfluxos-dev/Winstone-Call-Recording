package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(private val context: Context) {

    private val TAG = "AuthRepository"

    companion object {
        const val DEFAULT_CRM_BASE_URL = "https://crm.winstonebd.com/"
        private const val PREFS_FILE_NAME = "winstone_crm_auth_secure_prefs"

        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_AGENT_ID = "key_agent_id"
        private const val KEY_WORK_EMAIL = "key_work_email"
        private const val KEY_EMPLOYEE_ID = "key_employee_id"
        private const val KEY_FULL_NAME = "key_full_name"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_CRM_BASE_URL = "key_crm_base_url"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_LAST_LOGIN_TIMESTAMP = "key_last_login_timestamp"
        private const val KEY_DEVICE_ID = "key_device_id"
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences, falling back to standard private prefs", e)
            context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    private val _session = MutableStateFlow(loadStoredSession())
    val session: StateFlow<UserSession> = _session.asStateFlow()

    private val _crmBaseUrl = MutableStateFlow(loadStoredBaseUrl())
    val crmBaseUrl: StateFlow<String> = _crmBaseUrl.asStateFlow()

    private val _sessionExpiredEvent = MutableStateFlow(false)
    val sessionExpiredEvent: StateFlow<Boolean> = _sessionExpiredEvent.asStateFlow()

    private fun loadStoredBaseUrl(): String {
        return securePrefs.getString(KEY_CRM_BASE_URL, DEFAULT_CRM_BASE_URL) ?: DEFAULT_CRM_BASE_URL
    }

    private fun loadStoredSession(): UserSession {
        val isLoggedIn = securePrefs.getBoolean(KEY_IS_LOGGED_IN, true)
        val token = securePrefs.getString(KEY_ACCESS_TOKEN, "wn_sec_token_94719284") ?: "wn_sec_token_94719284"
        val agentId = securePrefs.getString(KEY_AGENT_ID, "agent_rahim_01") ?: "agent_rahim_01"
        val email = securePrefs.getString(KEY_WORK_EMAIL, "rahim.khan@winstonecrm.com") ?: "rahim.khan@winstonecrm.com"
        val empId = securePrefs.getString(KEY_EMPLOYEE_ID, "WN-88042") ?: "WN-88042"
        val fullName = securePrefs.getString(KEY_FULL_NAME, "Rahim Khan") ?: "Rahim Khan"
        val roleStr = securePrefs.getString(KEY_USER_ROLE, UserRole.AGENT.name) ?: UserRole.AGENT.name
        val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.AGENT }

        return UserSession(
            agentId = agentId,
            workEmail = email,
            employeeId = empId,
            fullName = fullName,
            role = role,
            accessToken = token,
            isLoggedIn = isLoggedIn
        )
    }

    fun getAuthToken(): String? {
        val token = securePrefs.getString(KEY_ACCESS_TOKEN, null)
        return if (!token.isNullOrBlank()) token else _session.value.accessToken
    }

    fun getDeviceId(): String {
        var devId = securePrefs.getString(KEY_DEVICE_ID, null)
        if (devId.isNullOrBlank()) {
            devId = "hyperx_hw1_${System.currentTimeMillis() % 100000}"
            securePrefs.edit().putString(KEY_DEVICE_ID, devId).apply()
        }
        return devId
    }

    fun getBaseUrl(): String {
        return _crmBaseUrl.value
    }

    fun setBaseUrl(url: String) {
        val formattedUrl = if (!url.endsWith("/")) "$url/" else url
        securePrefs.edit().putString(KEY_CRM_BASE_URL, formattedUrl).apply()
        _crmBaseUrl.value = formattedUrl
    }

    fun saveSession(session: UserSession, token: String, refreshToken: String? = null) {
        securePrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_AGENT_ID, session.agentId)
            .putString(KEY_WORK_EMAIL, session.workEmail)
            .putString(KEY_EMPLOYEE_ID, session.employeeId)
            .putString(KEY_FULL_NAME, session.fullName)
            .putString(KEY_USER_ROLE, session.role.name)
            .putLong(KEY_LAST_LOGIN_TIMESTAMP, System.currentTimeMillis())
            .apply()

        refreshToken?.let {
            securePrefs.edit().putString(KEY_REFRESH_TOKEN, it).apply()
        }

        _session.value = session.copy(isLoggedIn = true, accessToken = token)
        _sessionExpiredEvent.value = false
    }

    fun logout() {
        securePrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()

        _session.value = _session.value.copy(isLoggedIn = false, accessToken = "")
    }

    fun onSessionExpired() {
        Log.w(TAG, "CRM Session expired, triggering notification")
        _sessionExpiredEvent.value = true
    }

    fun resetSessionExpiredEvent() {
        _sessionExpiredEvent.value = false
    }

    fun isSessionValid(): Boolean {
        return securePrefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getAuthToken().isNullOrBlank()
    }
}
