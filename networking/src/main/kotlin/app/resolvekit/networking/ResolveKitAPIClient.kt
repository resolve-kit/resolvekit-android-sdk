package app.resolvekit.networking

import app.resolvekit.core.ResolveKitAPIClientError
import app.resolvekit.networking.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * HTTP client for all ResolveKit backend REST endpoints.
 *
 * All methods are suspend functions and switch to [Dispatchers.IO] internally.
 * Authentication uses Bearer token via [apiKeyProvider] on every request, plus
 * an optional [X-Resolvekit-Chat-Capability] header for session-scoped endpoints.
 */
@OptIn(ExperimentalSerializationApi::class)
class ResolveKitAPIClient(
    private val baseUrl: String,
    private val apiKeyProvider: () -> String?,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // -------------------------------------------------------------------------
    // SDK endpoints
    // -------------------------------------------------------------------------

    /** GET /v1/sdk/compat — verify this SDK version is supported. */
    suspend fun checkCompat(): SDKCompat = get("/v1/sdk/compat")

    /** GET /v1/sdk/chat-theme — fetch light/dark color palette. */
    suspend fun getChatTheme(): ChatTheme = get("/v1/sdk/chat-theme")

    // -------------------------------------------------------------------------
    // Function registration
    // -------------------------------------------------------------------------

    /** PUT /v1/functions/bulk — sync function definitions with the backend. */
    suspend fun registerFunctions(request: FunctionBulkRequest) {
        putUnit("/v1/functions/bulk", request)
    }

    // -------------------------------------------------------------------------
    // Session lifecycle
    // -------------------------------------------------------------------------

    /** POST /v1/sessions — create or reuse an active session. */
    suspend fun createSession(request: SessionCreateRequest): SessionResponse =
        post("/v1/sessions", request)

    /** PATCH /v1/sessions/{id}/context — update llm_context, functions, or locale without restarting. */
    suspend fun patchSessionContext(
        sessionId: String,
        token: String,
        request: SessionContextPatchRequest
    ): Unit = patchUnit("/v1/sessions/$sessionId/context", request, token)

    /** GET /v1/sessions/{id}/localization — fetch locale-specific UI strings. */
    suspend fun getLocalization(sessionId: String, token: String): SessionLocalization =
        get("/v1/sessions/$sessionId/localization", token)

    /** GET /v1/sessions/{id}/messages — retrieve conversation history. */
    suspend fun getMessages(sessionId: String, token: String): List<HistoryMessage> =
        get("/v1/sessions/$sessionId/messages", token)

    // -------------------------------------------------------------------------
    // Conversation
    // -------------------------------------------------------------------------

    /** POST /v1/sessions/{id}/messages — submit a user turn (returns HTTP 202). */
    suspend fun sendMessage(
        sessionId: String,
        token: String,
        request: MessageRequest
    ): MessageAccepted = post("/v1/sessions/$sessionId/messages", request, token)

    /** POST /v1/sessions/{id}/tool-results — submit a tool execution result. */
    suspend fun submitToolResult(
        sessionId: String,
        token: String,
        request: ToolResultRequest
    ): ToolResultResponse = post("/v1/sessions/$sessionId/tool-results", request, token)

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun apiKey(): String =
        apiKeyProvider()?.takeIf { it.isNotBlank() }
            ?: throw ResolveKitAPIClientError.MissingAPIKey

    private fun baseRequest(path: String, token: String? = null): Request.Builder =
        Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer ${apiKey()}")
            .apply { if (token != null) header("X-Resolvekit-Chat-Capability", token) }

    private suspend inline fun <reified T : Any> get(path: String, token: String? = null): T =
        withContext(Dispatchers.IO) {
            val request = baseRequest(path, token).get().build()
            executeAndParse<T>(request)
        }

    private suspend inline fun <reified T : Any, reified B : Any> post(
        path: String,
        body: B,
        token: String? = null
    ): T = withContext(Dispatchers.IO) {
        val bodyBytes = json.encodeToString(kotlinx.serialization.serializer<B>(), body)
            .toRequestBody(jsonMediaType)
        val request = baseRequest(path, token).post(bodyBytes).build()
        executeAndParse<T>(request)
    }

    private suspend inline fun <reified B : Any> putUnit(path: String, body: B) =
        withContext(Dispatchers.IO) {
            val bodyBytes = json.encodeToString(kotlinx.serialization.serializer<B>(), body)
                .toRequestBody(jsonMediaType)
            val request = baseRequest(path).put(bodyBytes).build()
            executeUnit(request)
        }

    private suspend inline fun <reified B : Any> patchUnit(
        path: String,
        body: B,
        token: String? = null
    ) = withContext(Dispatchers.IO) {
        val bodyBytes = json.encodeToString(kotlinx.serialization.serializer<B>(), body)
            .toRequestBody(jsonMediaType)
        val request = baseRequest(path, token).patch(bodyBytes).build()
        executeUnit(request)
    }

    private inline fun <reified T : Any> executeAndParse(request: Request): T {
        val response = httpClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        checkResponse(response, bodyStr)
        return json.decodeFromString(bodyStr)
    }

    private fun executeUnit(request: Request) {
        val response = httpClient.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        checkResponse(response, bodyStr)
    }

    private fun checkResponse(response: Response, body: String) {
        if (response.isSuccessful) return
        when (response.code) {
            401 -> throw ResolveKitAPIClientError.MissingAPIKey
            403 -> throw ResolveKitAPIClientError.ChatUnavailable
            405 -> throw ResolveKitAPIClientError.MethodNotAllowed(
                response.request.method, response.request.url.encodedPath
            )
            else -> throw ResolveKitAPIClientError.ServerError(response.code, body)
        }
    }
}
