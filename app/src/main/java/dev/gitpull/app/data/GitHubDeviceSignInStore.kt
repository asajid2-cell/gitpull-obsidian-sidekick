package dev.gitpull.app.data

import android.content.Context
import dev.gitpull.app.core.PendingGitHubSignIn

class GitHubDeviceSignInStore(context: Context) {
    private val prefs = context.getSharedPreferences("gitpull_github_device_sign_in", Context.MODE_PRIVATE)

    fun save(pending: PendingGitHubSignIn) {
        prefs.edit()
            .putString(KEY_DEVICE_CODE, pending.deviceCode)
            .putString(KEY_USER_CODE, pending.userCode)
            .putString(KEY_VERIFICATION_URI, pending.verificationUri)
            .putLong(KEY_EXPIRES_AT, pending.expiresAtMillis)
            .putInt(KEY_INTERVAL_SECONDS, pending.intervalSeconds)
            .apply()
    }

    fun load(nowMillis: Long = System.currentTimeMillis()): PendingGitHubSignIn? {
        val pending = PendingGitHubSignIn(
            deviceCode = prefs.getString(KEY_DEVICE_CODE, "").orEmpty(),
            userCode = prefs.getString(KEY_USER_CODE, "").orEmpty(),
            verificationUri = prefs.getString(KEY_VERIFICATION_URI, "").orEmpty(),
            expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT, 0L),
            intervalSeconds = prefs.getInt(KEY_INTERVAL_SECONDS, 5)
        )
        return if (pending.deviceCode.isBlank() || pending.userCode.isBlank() || pending.isExpired(nowMillis)) {
            clear()
            null
        } else {
            pending
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_DEVICE_CODE = "device_code"
        private const val KEY_USER_CODE = "user_code"
        private const val KEY_VERIFICATION_URI = "verification_uri"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_INTERVAL_SECONDS = "interval_seconds"
    }
}
