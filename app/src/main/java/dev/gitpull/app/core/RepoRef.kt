package dev.gitpull.app.core

data class RepoRef(
    val owner: String,
    val name: String,
    val branch: String = DEFAULT_BRANCH
) {
    init {
        require(owner.isNotBlank()) { "owner is required" }
        require(name.isNotBlank()) { "repo name is required" }
        require(branch.isNotBlank()) { "branch is required" }
    }

    val slug: String = "$owner/$name"

    companion object {
        const val DEFAULT_BRANCH = "main"
    }
}

object RepoUrlParser {
    private val repoRegex = Regex("^https://github\\.com/([^/]+)/([^/#?]+?)(?:\\.git)?/?(?:[?#].*)?$")

    fun parse(url: String, branch: String = RepoRef.DEFAULT_BRANCH): Result<RepoRef> {
        val trimmed = url.trim()
        val match = repoRegex.matchEntire(trimmed)
            ?: return Result.failure(IllegalArgumentException("Use https://github.com/owner/repo"))
        val owner = match.groupValues[1]
        val repo = match.groupValues[2].removeSuffix(".git")
        if (owner.isBlank() || repo.isBlank()) {
            return Result.failure(IllegalArgumentException("GitHub owner and repo are required"))
        }
        return Result.success(RepoRef(owner = owner, name = repo, branch = branch.ifBlank { RepoRef.DEFAULT_BRANCH }))
    }
}
