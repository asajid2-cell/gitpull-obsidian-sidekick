package dev.gitpull.app.core

data class PendingGitHubSignIn(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresAtMillis: Long,
    val intervalSeconds: Int
) {
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis >= expiresAtMillis
    }
}
