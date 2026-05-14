package dev.gitpull.app.core

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitHubArchiveClientTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun buildsCodeloadArchiveUrl() {
        val client = GitHubArchiveClient(baseUrl = "https://codeload.github.com")

        assertEquals(
            "https://codeload.github.com/owner/repo/zip/refs/heads/main",
            client.archiveUrl(RepoRef("owner", "repo"))
        )
    }

    @Test
    fun addsBearerTokenWhenConfigured() {
        val request = GitHubArchiveClient().request(RepoRef("owner", "repo"), "abc123")

        assertEquals("Bearer abc123", request.header("Authorization"))
        assertEquals("application/zip", request.header("Accept"))
    }

    @Test
    fun omitsBearerTokenWhenBlank() {
        val request = GitHubArchiveClient().request(RepoRef("owner", "repo"), "")

        assertNull(request.header("Authorization"))
    }

    @Test
    fun tokenBackedDownloadSendsAuthorizationHeader() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("zip-bytes"))
            server.start()

            val archive = temp.newFile("archive.zip")
            GitHubArchiveClient(baseUrl = server.url("/").toString())
                .downloadArchive(RepoRef("owner", "private-vault", "main"), "secret-token", archive)

            val request = server.takeRequest()
            assertEquals("/owner/private-vault/zip/refs/heads/main", request.path)
            assertEquals("Bearer secret-token", request.getHeader("Authorization"))
            assertEquals("application/zip", request.getHeader("Accept"))
            assertEquals("zip-bytes", archive.readText())
        }
    }
}
