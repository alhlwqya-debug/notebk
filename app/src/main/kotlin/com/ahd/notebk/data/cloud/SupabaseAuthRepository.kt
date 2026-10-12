package com.ahd.notebk.data.cloud

import android.content.Context
import android.util.Patterns
import com.ahd.notebk.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val label: String,
    val expiresAt: Long = 0L
)

sealed class AuthResult {
    data class EmailConfirmationRequired(val email: String) : AuthResult()
    data class ConfirmationEmailSent(val email: String) : AuthResult()
    data class SignedIn(val session: AuthSession) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

/**
 * Password authentication is intentionally used here. The previous OTP flow
 * is disabled until email/SMS delivery is configured correctly in Supabase.
 * Only the publishable key is embedded in the Android client.
 */
class SupabaseAuthRepository(private val context: Context) {
    private val preferences =
        context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    suspend fun signUpWithPassword(
        email: String,
        password: String,
        displayName: String = ""
    ): AuthResult =
        passwordRequest(
            endpoint = "/auth/v1/signup",
            email = email,
            password = password,
            isSignUp = true,
            displayName = displayName
        )

    suspend fun signInWithPassword(email: String, password: String): AuthResult =
        passwordRequest(
            endpoint = "/auth/v1/token?grant_type=password",
            email = email,
            password = password,
            isSignUp = false
        )

    private suspend fun passwordRequest(
        endpoint: String,
        email: String,
        password: String,
        isSignUp: Boolean,
        displayName: String = ""
    ): AuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val validationMessage = validate(normalizedEmail, password)
        if (validationMessage != null) {
            return@withContext AuthResult.Failure(validationMessage)
        }

        val payload = JSONObject().apply {
            put("email", normalizedEmail)
            put("password", password)
            if (isSignUp && displayName.isNotBlank()) {
                put("data", JSONObject().apply {
                    put("display_name", displayName.trim())
                })
            }
        }

        try {
            val response = postJson(endpoint, payload)
            if (response.code !in 200..299) {
                if (isEmailNotConfirmed(response.body)) {
                    return@withContext AuthResult.EmailConfirmationRequired(normalizedEmail)
                }
                return@withContext AuthResult.Failure(authError(response.body, response.code, isSignUp))
            }

            val session = sessionFromResponse(response.body, normalizedEmail)
            if (session != null) {
                saveSession(session)
                AuthResult.SignedIn(session)
            } else if (isSignUp) {
                // With Confirm email enabled Supabase creates the user but deliberately
                // omits the session until the address is verified.
                AuthResult.EmailConfirmationRequired(normalizedEmail)
            } else {
                AuthResult.Failure(context.getString(R.string.error_missing_session))
            }
        } catch (_: IOException) {
            AuthResult.Failure(context.getString(R.string.error_network))
        } catch (_: Exception) {
            AuthResult.Failure(context.getString(R.string.error_unknown))
        }
    }

    suspend fun resendConfirmation(email: String): AuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return@withContext AuthResult.Failure(context.getString(R.string.error_email_format))
        }

        val payload = JSONObject().apply {
            put("type", "signup")
            put("email", normalizedEmail)
        }

        try {
            val response = postJson("/auth/v1/resend", payload)
            if (response.code !in 200..299) {
                AuthResult.Failure(authError(response.body, response.code, false))
            } else {
                AuthResult.ConfirmationEmailSent(normalizedEmail)
            }
        } catch (_: IOException) {
            AuthResult.Failure(context.getString(R.string.error_network))
        } catch (_: Exception) {
            AuthResult.Failure(context.getString(R.string.error_unknown))
        }
    }

    private fun sessionFromResponse(body: String, label: String): AuthSession? {
        val json = JSONObject(body)
        val accessToken = json.optString("access_token")
        val refreshToken = json.optString("refresh_token")
        val userId = json.optJSONObject("user")?.optString("id").orEmpty()
        if (accessToken.isBlank() || userId.isBlank()) return null
        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            label = label,
            expiresAt = System.currentTimeMillis() + json.optLong("expires_in", 3600L) * 1000L
        )
    }

    private fun saveSession(session: AuthSession) {
        preferences.edit()
            .putString("access_token", session.accessToken)
            .putString("refresh_token", session.refreshToken)
            .putString("user_id", session.userId)
            .putString("label", session.label)
            .putLong("expires_at", session.expiresAt)
            .apply()
    }

    fun savedSession(): AuthSession? {
        val accessToken = preferences.getString("access_token", null) ?: return null
        val userId = preferences.getString("user_id", null) ?: return null
        return AuthSession(
            accessToken = accessToken,
            refreshToken = preferences.getString("refresh_token", "").orEmpty(),
            userId = userId,
            label = preferences.getString("label", "").orEmpty(),
            expiresAt = preferences.getLong("expires_at", 0L)
        )
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    /** Sends Supabase's password-reset email without exposing or storing a password. */
    suspend fun requestPasswordReset(email: String): AuthResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        if (!Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            return@withContext AuthResult.Failure(context.getString(R.string.error_email_format))
        }
        try {
            val response = postJson("/auth/v1/recover", JSONObject().put("email", normalizedEmail))
            if (response.code in 200..299) AuthResult.ConfirmationEmailSent(normalizedEmail)
            else AuthResult.Failure(authError(response.body, response.code, false))
        } catch (_: IOException) {
            AuthResult.Failure(context.getString(R.string.error_network))
        } catch (_: Exception) {
            AuthResult.Failure(context.getString(R.string.error_unknown))
        }
    }

    /** Updates the password for the currently authenticated Supabase user. */
    suspend fun changePassword(newPassword: String): AuthResult = withContext(Dispatchers.IO) {
        val session = savedSession()
            ?: return@withContext AuthResult.Failure("سجّل الدخول أولاً لتغيير كلمة المرور.")
        if (newPassword.length < 6) {
            return@withContext AuthResult.Failure(context.getString(R.string.error_password_short))
        }
        try {
            val response = authenticatedRequest(
                method = "PUT",
                endpoint = "/auth/v1/user",
                accessToken = session.accessToken,
                body = JSONObject().put("password", newPassword)
            )
            if (response.code in 200..299) AuthResult.SignedIn(session)
            else AuthResult.Failure(authError(response.body, response.code, false))
        } catch (_: IOException) {
            AuthResult.Failure(context.getString(R.string.error_network))
        } catch (_: Exception) {
            AuthResult.Failure(context.getString(R.string.error_unknown))
        }
    }

    /** Invalidates the remote session when possible and always clears local credentials. */
    suspend fun signOut(): AuthResult = withContext(Dispatchers.IO) {
        val session = savedSession()
        try {
            if (session != null && session.accessToken.isNotBlank()) {
                authenticatedRequest("POST", "/auth/v1/logout", session.accessToken, JSONObject())
            }
            clearSession()
            AuthResult.SignedIn(session ?: AuthSession("", "", "", ""))
        } catch (_: Exception) {
            // Local sign-out must still work when offline; never retain local tokens.
            clearSession()
            AuthResult.Failure("تم تسجيل الخروج من هذا الجهاز، لكن تعذر تأكيد إنهاء الجلسة السحابية.")
        }
    }

    /**
     * Account deletion must be performed by a trusted server-side Supabase Edge Function.
     * The service-role key must never be embedded in the Android application.
     */
    suspend fun deleteAccount(): AuthResult = withContext(Dispatchers.IO) {
        val session = savedSession()
            ?: return@withContext AuthResult.Failure("لا يوجد حساب مسجل الدخول.")
        try {
            val response = authenticatedRequest(
                method = "POST",
                endpoint = "/functions/v1/delete-account",
                accessToken = session.accessToken,
                body = JSONObject()
            )
            if (response.code in 200..299) {
                clearSession()
                AuthResult.SignedIn(session)
            } else {
                AuthResult.Failure(
                    if (response.code == 404) "حذف الحساب غير مفعّل على الخادم بعد. يلزم نشر دالة Supabase آمنة للحذف."
                    else authError(response.body, response.code, false)
                )
            }
        } catch (_: IOException) {
            AuthResult.Failure(context.getString(R.string.error_network))
        } catch (_: Exception) {
            AuthResult.Failure(context.getString(R.string.error_unknown))
        }
    }

    private fun authenticatedRequest(
        method: String,
        endpoint: String,
        accessToken: String,
        body: JSONObject
    ): HttpResponse {
        val connection = (URL("${SupabaseConfig.URL}$endpoint").openConnection() as HttpURLConnection)
        connection.requestMethod = method
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.doOutput = true
        connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        return HttpResponse(code, responseBody)
    }

    private fun validate(email: String, password: String): String? {
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                context.getString(R.string.error_email_format)
            password.length < 6 ->
                context.getString(R.string.error_password_short)
            else -> null
        }
    }

    private fun postJson(endpoint: String, body: JSONObject): HttpResponse {
        val connection = (URL("${SupabaseConfig.URL}$endpoint").openConnection() as HttpURLConnection)
        connection.requestMethod = "POST"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.doOutput = true
        connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }
        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        connection.disconnect()
        return HttpResponse(responseCode, responseBody)
    }

    private fun isEmailNotConfirmed(body: String): Boolean {
        val lower = body.lowercase(Locale.ROOT)
        return lower.contains("email_not_confirmed") ||
            lower.contains("email not confirmed") ||
            lower.contains("confirm your email") ||
            lower.contains("email address is not confirmed")
    }

    private fun authError(body: String, code: Int, isSignUp: Boolean): String {
        return try {
            val json = JSONObject(body)
            val errorCode = listOf("error_code", "code", "error")
                .asSequence()
                .map { json.optString(it).lowercase(Locale.ROOT) }
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
            val message = listOf("msg", "message", "error_description")
                .asSequence()
                .map { json.optString(it) }
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
            val lowerMessage = message.lowercase(Locale.ROOT)
            when {
                isEmailNotConfirmed(body) ->
                    context.getString(R.string.error_email_not_confirmed)
                errorCode == "invalid_credentials" || errorCode == "invalid_grant" ||
                    lowerMessage.contains("invalid login credentials") ->
                    context.getString(R.string.error_invalid_credentials)
                isSignUp && (errorCode == "user_already_exists" ||
                    errorCode == "email_exists" ||
                    lowerMessage.contains("already registered") ||
                    lowerMessage.contains("already exists")) ->
                    context.getString(R.string.error_email_already_registered)
                message.isNotBlank() -> message
                else -> context.getString(R.string.error_auth_with_code, code)
            }
        } catch (_: Exception) {
            context.getString(R.string.error_unknown)
        }
    }

    private data class HttpResponse(val code: Int, val body: String)
}

