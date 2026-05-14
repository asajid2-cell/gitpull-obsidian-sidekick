package dev.gitpull.app.core

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubOAuthClientTest {
    @Test
    fun requestDeviceCodeUsesGitHubDeviceFlow() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"device_code":"device-123","user_code":"ABCD-1234","verification_uri":"https://github.com/login/device","expires_in":900,"interval":5}"""))
            val client = GitHubOAuthClient(clientId = "client-id", githubBaseUrl = server.url("/").toString())

            val code = client.requestDeviceCode()

            assertEquals("device-123", code.deviceCode)
            assertEquals("ABCD-1234", code.userCode)
            assertEquals("https://github.com/login/device", code.verificationUri)
            val request = server.takeRequest()
            assertEquals("/login/device/code", request.path)
            assertEquals("POST", request.method)
            assertTrue(request.body.readUtf8().contains("client_id=client-id"))
        }
    }

    @Test
    fun pollDeviceCodeReturnsAccessToken() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"access_token":"oauth-token","token_type":"bearer","scope":"repo"}"""))
            val client = GitHubOAuthClient(clientId = "client-id", githubBaseUrl = server.url("/").toString())

            val token = client.pollDeviceCode("device-123")

            assertEquals("oauth-token", token)
            val request = server.takeRequest()
            assertEquals("/login/oauth/access_token", request.path)
            assertTrue(request.body.readUtf8().contains("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code"))
        }
    }

    @Test(expected = GitHubOAuthClient.AuthorizationPendingException::class)
    fun pollDeviceCodeReportsPendingAuthorization() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"error":"authorization_pending"}"""))
            GitHubOAuthClient(clientId = "client-id", githubBaseUrl = server.url("/").toString())
                .pollDeviceCode("device-123")
        }
    }
}
