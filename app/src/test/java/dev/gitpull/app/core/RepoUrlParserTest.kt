package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepoUrlParserTest {
    @Test
    fun parsesHttpsGitHubRepo() {
        val repo = RepoUrlParser.parse("https://github.com/ahmed/vault").getOrThrow()

        assertEquals("ahmed", repo.owner)
        assertEquals("vault", repo.name)
        assertEquals("main", repo.branch)
    }

    @Test
    fun stripsGitSuffixAndUsesCustomBranch() {
        val repo = RepoUrlParser.parse("https://github.com/ahmed/vault.git", "tablet").getOrThrow()

        assertEquals("vault", repo.name)
        assertEquals("tablet", repo.branch)
    }

    @Test
    fun rejectsNonGitHubUrl() {
        assertTrue(RepoUrlParser.parse("https://example.com/ahmed/vault").isFailure)
    }

    @Test
    fun rejectsMissingRepo() {
        assertTrue(RepoUrlParser.parse("https://github.com/ahmed").isFailure)
    }
}
