package dev.gitpull.app.core

data class AppConfig(
    val repoUrl: String = "",
    val branch: String = RepoRef.DEFAULT_BRANCH,
    val destinationTreeUri: String = "",
    val lastPulledAtMillis: Long = 0L,
    val lastStatus: String = "Ready to refresh",
    val tokenConfigured: Boolean = false
) {
    val isReady: Boolean get() = repoUrl.isNotBlank() && destinationTreeUri.isNotBlank()
}

data class PdfItem(
    val filename: String,
    val relativePath: String,
    val uriString: String = ""
)

data class PullConfig(
    val repo: RepoRef,
    val token: String?,
    val cacheDir: java.io.File
)

data class PullReport(
    val filesCopied: Int,
    val filesDeleted: Int,
    val pdfs: List<PdfItem>,
    val message: String,
    val manifest: SnapshotManifest = SnapshotManifest(emptyMap())
)
