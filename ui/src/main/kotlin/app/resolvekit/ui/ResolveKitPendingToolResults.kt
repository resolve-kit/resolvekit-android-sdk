package app.resolvekit.ui

import kotlinx.serialization.json.JsonElement
import java.util.UUID

data class PendingToolResult(
    val callId: String,
    val turnId: String,
    val status: String,
    val result: JsonElement? = null,
    val error: String? = null,
    val idempotencyKey: String = UUID.randomUUID().toString()
)

class ResolveKitPendingToolResults {
    private val payloads = LinkedHashMap<String, PendingToolResult>()

    fun enqueue(payload: PendingToolResult) {
        payloads[payload.callId] = payload
    }

    fun snapshot(): List<PendingToolResult> = payloads.values.toList()

    fun removeAll(callIds: Set<String>) {
        callIds.forEach(payloads::remove)
    }

    fun clear() {
        payloads.clear()
    }
}
