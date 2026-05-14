package dev.gitpull.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipFile

class GitHubLiveIntegrationTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun tokenListsPrivateFixtureRepoAndDownloadsArchive() {
        val token = System.getenv("GITPULL_LIVE_GITHUB_TOKEN").orEmpty()
        assumeTrue("Set GITPULL_LIVE_GITHUB_TOKEN to run live private GitHub validation", token.isNotBlank())

        val repoSlug = System.getenv("GITPULL_LIVE_REPO").orEmpty()
        assumeTrue("Set GITPULL_LIVE_REPO to owner/repo for live private GitHub validation", repoSlug.isNotBlank())
        val branch = System.getenv("GITPULL_LIVE_BRANCH").orEmpty().ifBlank { RepoRef.DEFAULT_BRANCH }
        val parts = repoSlug.split("/")
        assertEquals("GITPULL_LIVE_REPO must be owner/repo", 2, parts.size)

        val repos = GitHubRepositoryClient().listRepositories(token)
        val listedRepo = repos.firstOrNull { it.fullName == repoSlug }
        assertNotNull("Expected $repoSlug in authenticated GitHub repository browser response", listedRepo)
        assertEquals(branch, listedRepo!!.defaultBranch)
        assertTrue("Expected $repoSlug to be private for private-repo validation", listedRepo.privateRepo)

        val archive = temp.newFile("private-vault.zip")
        GitHubArchiveClient().downloadArchive(RepoRef(parts[0], parts[1], branch), token, archive)

        assertTrue("Expected downloaded archive to contain bytes", archive.length() > 0)
        val entries = ZipFile(archive).use { zip ->
            zip.entries().asSequence().map { it.name }.toList()
        }
        assertTrue("Archive should include Markdown", entries.any { it.endsWith(".md") })
        assertTrue("Archive should include Obsidian config", entries.any { it.contains("/.obsidian/") })
        assertTrue("Archive should include an image asset", entries.any { it.endsWith(".png") })
        assertTrue("Archive should include a PDF asset", entries.any { it.endsWith(".pdf") })

        val destination = temp.newFolder("private-vault-output")
        val service = PullService(
            archiveDownloader = { pullConfig, output ->
                GitHubArchiveClient().downloadArchive(pullConfig.repo, pullConfig.token, output)
            }
        )
        val report = service.pull(
            PullConfig(RepoRef(parts[0], parts[1], branch), token, temp.newFolder("cache")),
            FileSnapshotWriter(destination)
        )

        assertTrue("Pull should refresh Markdown into the destination", destination.walkTopDown().any { it.name.endsWith(".md") })
        assertTrue("Pull should refresh Obsidian config into the destination", destination.resolve(".obsidian").isDirectory)
        assertTrue("Pull should refresh image assets into the destination", destination.walkTopDown().any { it.name.endsWith(".png") })
        assertEquals(listOf("Papers/Gitpull-fixture.pdf"), report.pdfs.map { it.relativePath })
    }
}
