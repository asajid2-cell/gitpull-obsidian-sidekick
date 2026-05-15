package dev.gitpull.app.core

import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Int,
    val intervalSeconds: Int
)

interface GitHubOAuthGateway {
    val isConfigured: Boolean
    fun requestDeviceCode(scope: String = "repo"): GitHubDeviceCode
    fun pollDeviceCode(deviceCode: String): String
}

class GitHubOAuthClient(
    private val clientId: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val githubBaseUrl: String = "https://github.com"
) : GitHubOAuthGateway {
    override val isConfigured: Boolean get() = clientId.isNotBlank()

    override fun requestDeviceCode(scope: String): GitHubDeviceCode {
        require(isConfigured) { "GitHub browser login is not configured for this build" }
        val body = FormBody.Builder()
            .add("client_id", clientId)
            .add("scope", scope)
            .build()
        val request = Request.Builder()
            .url(githubBaseUrl.trimEnd('/').toHttpUrl().newBuilder().addPathSegments("login/device/code").build())
            .post(body)
            .header("Accept", "application/json")
            .header("User-Agent", "gitpull-android")
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("GitHub sign-in failed: HTTP ${response.code}")
            }
            val error = stringField(text, "error_description") ?: stringField(text, "error")
            if (!error.isNullOrBlank()) throw IllegalStateException(error)
            return GitHubDeviceCode(
                deviceCode = stringField(text, "device_code") ?: throw IllegalStateException("GitHub did not return a device code"),
                userCode = stringField(text, "user_code") ?: throw IllegalStateException("GitHub did not return a user code"),
                verificationUri = stringField(text, "verification_uri") ?: "https://github.com/login/device",
                expiresInSeconds = intField(text, "expires_in") ?: 900,
                intervalSeconds = intField(text, "interval") ?: 5
            )
        }
    }

    override fun pollDeviceCode(deviceCode: String): String {
        require(isConfigured) { "GitHub browser login is not configured for this build" }
        require(deviceCode.isNotBlank()) { "GitHub login did not return a device code" }

        val body = FormBody.Builder()
            .add("client_id", clientId)
            .add("device_code", deviceCode)
            .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            .build()
        val request = Request.Builder()
            .url(githubBaseUrl.trimEnd('/').toHttpUrl().newBuilder().addPathSegments("login/oauth/access_token").build())
            .post(body)
            .header("Accept", "application/json")
            .header("User-Agent", "gitpull-android")
            .build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("GitHub login failed: HTTP ${response.code}")
            }
            val error = stringField(text, "error")
            if (error == "authorization_pending" || error == "slow_down") throw AuthorizationPendingException(error)
            val description = stringField(text, "error_description")
            if (!error.isNullOrBlank()) throw OAuthException(error, description)
            return stringField(text, "access_token")
                ?: throw IllegalStateException("GitHub login did not return an access token")
        }
    }

    class AuthorizationPendingException(message: String) : RuntimeException(message)
    class OAuthException(val code: String, description: String?) : RuntimeException(
        listOfNotNull(code, description).joinToString(": ")
    )

    companion object {
        private fun stringField(jsonObject: String, name: String): String? {
            val match = Regex("\"${Regex.escape(name)}\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").find(jsonObject)
            return match?.groupValues?.get(1)
                ?.replace("\\/", "/")
                ?.replace("\\\"", "\"")
                ?.replace("\\n", "\n")
        }

        private fun intField(jsonObject: String, name: String): Int? {
            return Regex("\"${Regex.escape(name)}\"\\s*:\\s*(\\d+)").find(jsonObject)?.groupValues?.get(1)?.toIntOrNull()
        }
    }
}
