package app.resolvekit.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ClientContext(
    val platform: String = "android",
    @SerialName("os_name") val osName: String = "Android",
    @SerialName("os_version") val osVersion: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("app_build") val appBuild: String,
    @SerialName("sdk_name") val sdkName: String = "resolvekit-android-sdk",
    @SerialName("sdk_version") val sdkVersion: String = "1.0.0"
)

@Serializable
data class SessionCreateRequest(
    @SerialName("device_id") val deviceId: String?,
    val client: ClientContext,
    @SerialName("llm_context") val llmContext: Map<String, JsonElement> = emptyMap(),
    @SerialName("available_function_names") val availableFunctionNames: List<String>? = null,
    val locale: String? = null,
    @SerialName("preferred_locales") val preferredLocales: List<String>? = null,
    @SerialName("reuse_active_session") val reuseActiveSession: Boolean = true
)

@Serializable
data class SessionResponse(
    val id: String,
    @SerialName("app_id") val appId: String,
    @SerialName("device_id") val deviceId: String?,
    @SerialName("events_url") val eventsUrl: String,
    @SerialName("chat_capability_token") val chatCapabilityToken: String,
    @SerialName("reused_active_session") val reusedActiveSession: Boolean,
    @SerialName("chat_title") val chatTitle: String? = null,
    @SerialName("message_placeholder") val messagePlaceholder: String? = null,
    @SerialName("initial_message") val initialMessage: String? = null,
    val locale: String? = null
)

@Serializable
data class SessionContextPatchRequest(
    @SerialName("llm_context") val llmContext: Map<String, JsonElement>? = null,
    @SerialName("available_function_names") val availableFunctionNames: List<String>? = null,
    val locale: String? = null
)

@Serializable
data class MessageRequest(
    val text: String,
    @SerialName("request_id") val requestId: String,
    val locale: String? = null
)

@Serializable
data class MessageAccepted(
    @SerialName("turn_id") val turnId: String,
    @SerialName("request_id") val requestId: String,
    val status: String
)

@Serializable
data class ToolResultRequest(
    @SerialName("turn_id") val turnId: String,
    @SerialName("idempotency_key") val idempotencyKey: String,
    @SerialName("call_id") val callId: String,
    val status: String, // "success" | "error"
    val result: JsonElement? = null,
    val error: String? = null
)

@Serializable
data class ToolResultResponse(
    val status: String,
    val deduplicated: Boolean = false
)

@Serializable
data class SessionLocalization(
    val locale: String,
    @SerialName("chat_title") val chatTitle: String,
    @SerialName("message_placeholder") val messagePlaceholder: String,
    @SerialName("initial_message") val initialMessage: String? = null
)

@Serializable
data class SDKCompat(
    @SerialName("minimum_sdk_version") val minimumSdkVersion: String,
    @SerialName("supported_sdk_major_versions") val supportedSdkMajorVersions: List<Int>
)

@Serializable
data class ChatThemePalette(
    @SerialName("screenBackground") val screenBackground: String,
    @SerialName("titleText") val titleText: String,
    @SerialName("statusText") val statusText: String,
    @SerialName("composerBackground") val composerBackground: String,
    @SerialName("composerText") val composerText: String,
    @SerialName("composerPlaceholder") val composerPlaceholder: String,
    @SerialName("userBubbleBackground") val userBubbleBackground: String,
    @SerialName("userBubbleText") val userBubbleText: String,
    @SerialName("assistantBubbleBackground") val assistantBubbleBackground: String,
    @SerialName("assistantBubbleText") val assistantBubbleText: String,
    @SerialName("loaderBubbleBackground") val loaderBubbleBackground: String,
    @SerialName("loaderDotActive") val loaderDotActive: String,
    @SerialName("loaderDotInactive") val loaderDotInactive: String,
    @SerialName("toolCardBackground") val toolCardBackground: String,
    @SerialName("toolCardBorder") val toolCardBorder: String,
    @SerialName("toolCardTitle") val toolCardTitle: String,
    @SerialName("toolCardBody") val toolCardBody: String
)

@Serializable
data class ChatTheme(
    val light: ChatThemePalette,
    val dark: ChatThemePalette
)

@Serializable
data class HistoryMessage(
    val id: String,
    val role: String,
    val content: String?,
    @SerialName("sequence_number") val sequenceNumber: Int,
    @SerialName("created_at") val createdAt: String
)
