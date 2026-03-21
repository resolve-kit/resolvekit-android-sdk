package app.resolvekit.networking

import app.resolvekit.core.ResolveKitAPIClientError
import app.resolvekit.networking.models.*
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResolveKitAPIClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: ResolveKitAPIClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        client = ResolveKitAPIClient(
            baseUrl = server.url("/").toString(),
            apiKeyProvider = { "test-api-key" }
        )
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `checkCompat returns parsed response`() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""{"minimum_sdk_version":"1.0.0","supported_sdk_major_versions":[1]}""")
                .addHeader("Content-Type", "application/json")
        )
        val result = client.checkCompat()
        assertEquals("1.0.0", result.minimumSdkVersion)
        assertEquals(listOf(1), result.supportedSdkMajorVersions)
    }

    @Test
    fun `getChatTheme parses current backend response shape`() = runTest {
        server.enqueue(
            MockResponse()
                .setBody(
                    """
                    {
                      "light": {
                        "screenBackground": "#F7F7FA",
                        "titleText": "#111827",
                        "statusText": "#4B5563",
                        "composerBackground": "#FFFFFF",
                        "composerText": "#111827",
                        "composerPlaceholder": "#9CA3AF",
                        "userBubbleBackground": "#DBEAFE",
                        "userBubbleText": "#1E3A8A",
                        "assistantBubbleBackground": "#E5E7EB",
                        "assistantBubbleText": "#111827",
                        "loaderBubbleBackground": "#E5E7EB",
                        "loaderDotActive": "#374151",
                        "loaderDotInactive": "#9CA3AF",
                        "toolCardBackground": "#FFFFFFCC",
                        "toolCardBorder": "#D1D5DB",
                        "toolCardTitle": "#111827",
                        "toolCardBody": "#374151"
                      },
                      "dark": {
                        "screenBackground": "#0B0C10",
                        "titleText": "#E5E7EB",
                        "statusText": "#9CA3AF",
                        "composerBackground": "#111318",
                        "composerText": "#E5E7EB",
                        "composerPlaceholder": "#6B7280",
                        "userBubbleBackground": "#1E3A8A99",
                        "userBubbleText": "#DBEAFE",
                        "assistantBubbleBackground": "#1F2937",
                        "assistantBubbleText": "#E5E7EB",
                        "loaderBubbleBackground": "#1F2937",
                        "loaderDotActive": "#E5E7EB",
                        "loaderDotInactive": "#6B7280",
                        "toolCardBackground": "#111318CC",
                        "toolCardBorder": "#374151",
                        "toolCardTitle": "#E5E7EB",
                        "toolCardBody": "#9CA3AF"
                      }
                    }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        val result = client.getChatTheme()

        assertEquals("#DBEAFE", result.light.userBubbleBackground)
        assertEquals("#0B0C10", result.dark.screenBackground)
    }

    @Test
    fun `createSession sends correct request and parses response`() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""
                    {
                      "id":"sess-1","app_id":"app-1","device_id":null,
                      "events_url":"/v1/sessions/sess-1/events",
                      "chat_capability_token":"tok","reused_active_session":false
                    }
                """.trimIndent())
                .addHeader("Content-Type", "application/json")
        )
        val req = SessionCreateRequest(
            deviceId = "dev-1",
            client = ClientContext(osVersion = "13", appVersion = "1.0", appBuild = "1")
        )
        val result = client.createSession(req)
        assertEquals("sess-1", result.id)
        assertEquals("tok", result.chatCapabilityToken)
        assertFalse(result.reusedActiveSession)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/v1/sessions", recorded.path)
        assertEquals("Bearer test-api-key", recorded.getHeader("Authorization"))
    }

    @Test
    fun `sendMessage sends correct headers`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setBody("""{"turn_id":"t1","request_id":"r1","status":"accepted"}""")
                .addHeader("Content-Type", "application/json")
        )
        val result = client.sendMessage("sess-1", "cap-token", MessageRequest("hello", "r1"))
        assertEquals("t1", result.turnId)
        assertEquals("accepted", result.status)

        val recorded = server.takeRequest()
        assertEquals("cap-token", recorded.getHeader("X-Resolvekit-Chat-Capability"))
    }

    @Test
    fun `throws MissingAPIKey when provider returns null`() = runTest {
        val noKeyClient = ResolveKitAPIClient(
            baseUrl = server.url("/").toString(),
            apiKeyProvider = { null }
        )
        try {
            noKeyClient.checkCompat()
            fail("Expected MissingAPIKey")
        } catch (e: ResolveKitAPIClientError.MissingAPIKey) {
            // expected
        }
    }

    @Test
    fun `throws ChatUnavailable on 403`() = runTest {
        server.enqueue(MockResponse().setResponseCode(403))
        try {
            client.checkCompat()
            fail("Expected ChatUnavailable")
        } catch (e: ResolveKitAPIClientError.ChatUnavailable) {
            // expected
        }
    }

    @Test
    fun `throws ServerError on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Error"))
        try {
            client.checkCompat()
            fail("Expected ServerError")
        } catch (e: ResolveKitAPIClientError.ServerError) {
            assertEquals(500, e.statusCode)
        }
    }
}
