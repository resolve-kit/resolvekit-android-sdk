package app.resolvekit.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EventEnvelope(
    @SerialName("event_id") val eventId: String,
    @SerialName("turn_id") val turnId: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    val timestamp: String? = null,
    val type: String,
    val payload: JsonElement
)

@Serializable
data class TextDeltaPayload(
    val delta: String,
    val accumulated: String
)

@Serializable
data class ToolCallRequestPayload(
    @SerialName("call_id") val callId: String,
    @SerialName("function_name") val functionName: String,
    val arguments: JsonElement,
    @SerialName("timeout_seconds") val timeoutSeconds: Int = 30,
    @SerialName("human_description") val humanDescription: String = "",
    @SerialName("requires_approval") val requiresApproval: Boolean = true
)

@Serializable
data class TurnCompletePayload(
    @SerialName("full_text") val fullText: String
)

@Serializable
data class ServerErrorPayload(
    val code: String,
    val message: String,
    val recoverable: Boolean = true
)

sealed class ResolveKitEvent {
    data class TextDelta(
        val eventId: String,
        val turnId: String?,
        val delta: String,
        val accumulated: String
    ) : ResolveKitEvent()

    data class ToolCallRequest(
        val eventId: String,
        val turnId: String?,
        val payload: ToolCallRequestPayload
    ) : ResolveKitEvent()

    data class TurnComplete(
        val eventId: String,
        val turnId: String?,
        val fullText: String
    ) : ResolveKitEvent()

    data class ServerError(
        val eventId: String,
        val code: String,
        val message: String,
        val recoverable: Boolean
    ) : ResolveKitEvent()

    data class Unknown(
        val eventId: String,
        val type: String
    ) : ResolveKitEvent()
}
