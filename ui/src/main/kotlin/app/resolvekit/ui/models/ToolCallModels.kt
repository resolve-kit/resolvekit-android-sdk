package app.resolvekit.ui.models

import java.util.Date
import java.util.UUID

enum class ResolveKitToolCallItemStatus {
    PENDING_APPROVAL,
    RUNNING,
    COMPLETED,
    CANCELLED,
    FAILED
}

data class ToolCallChecklistItem(
    val id: String,
    val functionName: String,
    val humanDescription: String,
    val timeoutSeconds: Int,
    val requiresApproval: Boolean,
    val rawArguments: String = "{}",
    var status: ResolveKitToolCallItemStatus = ResolveKitToolCallItemStatus.PENDING_APPROVAL,
    val createdAt: Date = Date()
)

enum class ResolveKitToolCallBatchState {
    IDLE,
    AWAITING_APPROVAL,
    APPROVED,
    DECLINED,
    EXECUTING,
    FINISHED
}

data class ToolCallChecklistBatch(
    val id: UUID = UUID.randomUUID(),
    val items: List<ToolCallChecklistItem>,
    var state: ResolveKitToolCallBatchState = ResolveKitToolCallBatchState.IDLE,
    val createdAt: Date = Date()
)

enum class ResolveKitConnectionState {
    IDLE,
    REGISTERING,
    CONNECTING,
    ACTIVE,
    RECONNECTING,
    RECONNECTED,
    FAILED,
    BLOCKED
}

enum class ResolveKitAppearanceMode { SYSTEM, LIGHT, DARK }
