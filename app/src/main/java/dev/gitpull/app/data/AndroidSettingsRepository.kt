package dev.gitpull.app.data

import android.content.Context
import dev.gitpull.app.core.AppConfig
import dev.gitpull.app.core.RepoRef
import dev.gitpull.app.core.SnapshotManifest
import dev.gitpull.app.core.SnapshotManifestCodec

class AndroidSettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("gitpull_settings", Context.MODE_PRIVATE)

    fun load(): AppConfig {
        return AppConfig(
            repoUrl = prefs.getString(KEY_REPO_URL, "") ?: "",
            branch = prefs.getString(KEY_BRANCH, RepoRef.DEFAULT_BRANCH) ?: RepoRef.DEFAULT_BRANCH,
            destinationTreeUri = prefs.getString(KEY_DESTINATION_URI, "") ?: "",
            lastPulledAtMillis = prefs.getLong(KEY_LAST_PULLED, 0L),
            lastStatus = prefs.getString(KEY_LAST_STATUS, "Ready to refresh") ?: "Ready to refresh",
            tokenConfigured = prefs.getBoolean(KEY_TOKEN_CONFIGURED, false)
        )
    }

    fun save(config: AppConfig) {
        prefs.edit()
            .putString(KEY_REPO_URL, config.repoUrl)
            .putString(KEY_BRANCH, config.branch.ifBlank { RepoRef.DEFAULT_BRANCH })
            .putString(KEY_DESTINATION_URI, config.destinationTreeUri)
            .putLong(KEY_LAST_PULLED, config.lastPulledAtMillis)
            .putString(KEY_LAST_STATUS, config.lastStatus)
            .putBoolean(KEY_TOKEN_CONFIGURED, config.tokenConfigured)
            .commit()
    }

    fun clearSnapshotMetadata() {
        prefs.edit()
            .putLong(KEY_LAST_PULLED, 0L)
            .putString(KEY_LAST_STATUS, "Ready to refresh")
            .remove(KEY_LAST_MANIFEST)
            .apply()
    }

    fun loadLastManifest(): SnapshotManifest {
        return SnapshotManifestCodec.decode(prefs.getString(KEY_LAST_MANIFEST, "") ?: "")
    }

    fun saveLastManifest(manifest: SnapshotManifest) {
        prefs.edit().putString(KEY_LAST_MANIFEST, SnapshotManifestCodec.encode(manifest)).apply()
    }

    companion object {
        private const val KEY_REPO_URL = "repo_url"
        private const val KEY_BRANCH = "branch"
        private const val KEY_DESTINATION_URI = "destination_uri"
        private const val KEY_LAST_PULLED = "last_pulled"
        private const val KEY_LAST_STATUS = "last_status"
        private const val KEY_TOKEN_CONFIGURED = "token_configured"
        private const val KEY_LAST_MANIFEST = "last_manifest"
    }
}
