package dev.gitpull.app.core

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class GitHubArchiveClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = "https://codeload.github.com"
) {
    fun archiveUrl(repo: RepoRef): String {
        return "${baseUrl.trimEnd('/')}/${repo.owner}/${repo.name}/zip/refs/heads/${repo.branch}"
    }

    fun request(repo: RepoRef, token: String?): Request {
        val builder = Request.Builder()
            .url(archiveUrl(repo))
            .get()
            .header("Accept", "application/zip")
            .header("User-Agent", "gitpull-android")
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        return builder.build()
    }

    fun downloadArchive(repo: RepoRef, token: String?, destination: File): File {
        destination.parentFile?.mkdirs()
        client.newCall(request(repo, token)).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("GitHub archive download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("GitHub archive response had no body")
            destination.outputStream().use { out ->
                body.byteStream().use { input -> input.copyTo(out) }
            }
        }
        return destination
    }
}
