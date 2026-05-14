package dev.gitpull.app.core

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

data class GitHubRepository(
    val fullName: String,
    val htmlUrl: String,
    val defaultBranch: String,
    val privateRepo: Boolean
)

class GitHubRepositoryClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val apiBaseUrl: String = "https://api.github.com"
) {
    fun request(token: String, page: Int = 1): Request {
        require(token.isNotBlank()) { "GitHub token is required to browse repositories" }
        val url = apiBaseUrl.trimEnd('/').toHttpUrl().newBuilder()
            .addPathSegments("user/repos")
            .addQueryParameter("per_page", "100")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("affiliation", "owner,collaborator,organization_member")
            .addQueryParameter("sort", "updated")
            .build()
        return Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer $token")
            .header("User-Agent", "gitpull-android")
            .build()
    }

    fun listRepositories(token: String): List<GitHubRepository> {
        val all = mutableListOf<GitHubRepository>()
        for (page in 1..10) {
            val pageItems = fetchPage(token, page)
            all += pageItems
            if (pageItems.size < 100) break
        }
        return all
    }

    private fun fetchPage(token: String, page: Int): List<GitHubRepository> {
        client.newCall(request(token, page)).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("GitHub repository list failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("GitHub repository response had no body")
            return parseRepositories(body.string())
        }
    }

    fun parseRepositories(json: String): List<GitHubRepository> {
        return splitTopLevelObjects(json).mapNotNull { item ->
            val fullName = stringField(item, "full_name") ?: return@mapNotNull null
            val htmlUrl = stringField(item, "html_url") ?: "https://github.com/$fullName"
            val defaultBranch = stringField(item, "default_branch") ?: RepoRef.DEFAULT_BRANCH
            GitHubRepository(
                fullName = fullName,
                htmlUrl = htmlUrl,
                defaultBranch = defaultBranch,
                privateRepo = booleanField(item, "private")
            )
        }.toList()
    }

    private fun stringField(jsonObject: String, name: String): String? {
        val match = Regex("\"${Regex.escape(name)}\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").find(jsonObject)
        return match?.groupValues?.get(1)?.replace("\\/", "/")?.replace("\\\"", "\"")
    }

    private fun booleanField(jsonObject: String, name: String): Boolean {
        return Regex("\"${Regex.escape(name)}\"\\s*:\\s*true").containsMatchIn(jsonObject)
    }

    private fun splitTopLevelObjects(json: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1
        var inString = false
        var escaping = false

        json.forEachIndexed { index, char ->
            if (inString) {
                if (escaping) {
                    escaping = false
                } else if (char == '\\') {
                    escaping = true
                } else if (char == '"') {
                    inString = false
                }
                return@forEachIndexed
            }

            when (char) {
                '"' -> inString = true
                '{' -> {
                    if (depth == 0) start = index
                    depth += 1
                }
                '}' -> {
                    depth -= 1
                    if (depth == 0 && start >= 0) {
                        objects += json.substring(start, index + 1)
                        start = -1
                    }
                }
            }
        }

        return objects
    }
}
