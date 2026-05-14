package dev.gitpull.app.core

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubRepositoryClientTest {
    @Test
    fun buildsAuthenticatedRepositoryListRequest() {
        val client = GitHubRepositoryClient(apiBaseUrl = "https://api.github.test")

        val request = client.request("token-123", page = 2)

        assertEquals("Bearer token-123", request.header("Authorization"))
        assertEquals("application/vnd.github+json", request.header("Accept"))
        assertEquals("/user/repos", request.url.encodedPath)
        assertEquals("2", request.url.queryParameter("page"))
        assertEquals("100", request.url.queryParameter("per_page"))
    }

    @Test
    fun parsesRepositoryList() {
        val client = GitHubRepositoryClient()

        val repos = client.parseRepositories(
            """
            [
              {
                "owner": {
                  "login": "example-user",
                  "id": 1234
                },
                "full_name": "example-user/example-vault-fixture",
                "html_url": "https://github.com/example-user/example-vault-fixture",
                "default_branch": "main",
                "private": true,
                "permissions": {
                  "admin": true,
                  "push": true,
                  "pull": true
                }
              },
              {
                "full_name": "octocat/Hello-World",
                "html_url": "https://github.com/octocat/Hello-World",
                "default_branch": "master",
                "private": false
              }
            ]
            """.trimIndent()
        )

        assertEquals(2, repos.size)
        assertEquals("example-user/example-vault-fixture", repos[0].fullName)
        assertEquals("https://github.com/example-user/example-vault-fixture", repos[0].htmlUrl)
        assertEquals("main", repos[0].defaultBranch)
        assertEquals(true, repos[0].privateRepo)
        assertEquals("master", repos[1].defaultBranch)
        assertEquals(false, repos[1].privateRepo)
    }

    @Test
    fun listRepositoriesSendsBearerToken() {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(
                        """
                        [
                          {
                            "full_name": "owner/vault",
                            "html_url": "https://github.com/owner/vault",
                            "default_branch": "main",
                            "private": true
                          }
                        ]
                        """.trimIndent()
                    )
            )
            server.start()

            val repos = GitHubRepositoryClient(apiBaseUrl = server.url("/").toString())
                .listRepositories("secret-token")

            val request = server.takeRequest()
            assertEquals("/user/repos?per_page=100&page=1&affiliation=owner%2Ccollaborator%2Corganization_member&sort=updated", request.path)
            assertEquals("Bearer secret-token", request.getHeader("Authorization"))
            assertEquals("owner/vault", repos.single().fullName)
        }
    }
}
