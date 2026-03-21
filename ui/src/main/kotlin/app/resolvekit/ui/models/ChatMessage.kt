package app.resolvekit.ui.models

import java.util.Date
import java.util.UUID

enum class ChatMessageRole { USER, ASSISTANT, SYSTEM }

data class ResolveKitChatMessage(
    val id: UUID = UUID.randomUUID(),
    val role: ChatMessageRole,
    val text: String,
    val createdAt: Date = Date()
)
