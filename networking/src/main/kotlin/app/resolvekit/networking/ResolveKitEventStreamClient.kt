package app.resolvekit.networking

import android.util.Log
import app.resolvekit.core.ResolveKitAPIClientError
import app.resolvekit.networking.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.sse.*
import java.util.concurrent.TimeUnit

/**
 * Connects to the backend Server-Sent Events stream for a session and exposes
 * events as a [Flow]. Reconnection and cursor management are handled by the
 * caller ([ResolveKitRuntime]).
 */
class ResolveKitEventStreamClient(
    private val baseUrl: String,
    private val apiKeyProvider: () -> String?,
    httpClient: OkHttpClient? = null
) {
    private val http = httpClient ?: OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.SECONDS)   // keep-alive; no read timeout
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Open an SSE stream at [eventsPath] and emit [ResolveKitEvent] items.
     *
     * @param eventsPath  Path returned by the session response, e.g. `/v1/sessions/{id}/events`
     * @param chatToken   Chat capability token for the session
     * @param cursor      Last seen event_id for reconnection replay; null for fresh connection
     */
    fun stream(
        eventsPath: String,
        chatToken: String,
        cursor: String? = null
    ): Flow<ResolveKitEvent> = callbackFlow {
        val url = buildString {
            append(baseUrl.trimEnd('/'))
            append(eventsPath)
            if (cursor != null) append("?cursor=$cursor")
        }

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer ${apiKeyProvider() ?: error("No API key")}")
            .header("X-Resolvekit-Chat-Capability", chatToken)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        Log.d("ResolveKit.SSE", "Connecting to $url (cursor=$cursor)")
        val factory = EventSources.createFactory(http)
        val source = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("ResolveKit.SSE", "Stream opened: HTTP ${response.code}")
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                Log.d("ResolveKit.SSE", "Raw event id=$id type=$type data=${data.take(120)}")
                parseEvent(data)?.let { trySend(it) }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                Log.e("ResolveKit.SSE", "Stream failure HTTP ${response?.code}: ${t?.message}", t)
                close(mapStreamFailure(t, response))
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("ResolveKit.SSE", "Stream closed cleanly")
                close()
            }
        })

        awaitClose { source.cancel() }
    }

    private fun parseEvent(data: String): ResolveKitEvent? {
        val envelope = runCatching { json.decodeFromString<EventEnvelope>(data) }.getOrNull()
            ?: return null

        return when (envelope.type) {
            "assistant_text_delta" -> runCatching {
                val p = json.decodeFromJsonElement<TextDeltaPayload>(envelope.payload)
                ResolveKitEvent.TextDelta(envelope.eventId, envelope.turnId, p.delta, p.accumulated)
            }.getOrNull()

            "tool_call_request" -> runCatching {
                val p = json.decodeFromJsonElement<ToolCallRequestPayload>(envelope.payload)
                ResolveKitEvent.ToolCallRequest(envelope.eventId, envelope.turnId, p)
            }.getOrNull()

            "turn_complete" -> runCatching {
                val p = json.decodeFromJsonElement<TurnCompletePayload>(envelope.payload)
                ResolveKitEvent.TurnComplete(envelope.eventId, envelope.turnId, p.fullText)
            }.getOrNull()

            "error" -> runCatching {
                val p = json.decodeFromJsonElement<ServerErrorPayload>(envelope.payload)
                ResolveKitEvent.ServerError(envelope.eventId, p.code, p.message, p.recoverable)
            }.getOrNull()

            else -> ResolveKitEvent.Unknown(envelope.eventId, envelope.type)
        }
    }

    private fun mapStreamFailure(t: Throwable?, response: Response?): Throwable {
        val statusCode = response?.code
        return when (statusCode) {
            401 -> ResolveKitAPIClientError.MissingAPIKey
            403 -> ResolveKitAPIClientError.ChatUnavailable
            405 -> ResolveKitAPIClientError.MethodNotAllowed(
                response.request.method,
                response.request.url.encodedPath
            )
            null -> t ?: Exception("SSE stream failed")
            else -> ResolveKitAPIClientError.ServerError(
                statusCode,
                "SSE stream failed: HTTP $statusCode"
            )
        }
    }

    companion object {
        /** Parse SSE data lines for testing without a live server. */
        internal fun parseEventData(data: String): ResolveKitEvent? =
            ResolveKitEventStreamClient("", { null }).parseEvent(data)
    }
}
