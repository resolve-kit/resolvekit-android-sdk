package app.resolvekit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.resolvekit.ui.*
import app.resolvekit.ui.models.ResolveKitToolCallBatchState
import app.resolvekit.ui.models.ResolveKitToolCallItemStatus
import app.resolvekit.ui.models.ToolCallChecklistItem

/**
 * Demonstrates the tool-call approval flow.
 *
 * The sample app includes DeleteData — a function with
 * `requiresApproval = true` — which triggers the approval UI
 * before execution.
 *
 * This activity walks through:
 * 1. Starting a chat session
 * 2. Triggering a tool call that requires approval
 * 3. Observing the approval checklist UI
 * 4. Approving/denying the tool call
 */
class ApprovalDemoActivity : ComponentActivity() {

    private val runtime by lazy {
        ResolveKitRuntime(
            configuration = ResolveKitConfiguration(
                baseUrl = BuildConfig.RESOLVEKIT_BASE_URL,
                apiKeyProvider = { BuildConfig.RESOLVEKIT_API_KEY.takeIf { it.isNotBlank() } },
                functions = listOf(
                    GetCurrentTime,
                    AddNumbers,
                    DeleteData  // requiresApproval = true
                )
            ),
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ApprovalDemoScreen(
                    runtime = runtime,
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runtime.stop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalDemoScreen(
    runtime: ResolveKitRuntime,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by runtime.connectionState.collectAsState()
    val chatTitle by runtime.chatTitle.collectAsState()
    val toolCallBatchState by runtime.toolCallBatchState.collectAsState()
    val toolCallChecklist by runtime.toolCallChecklist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                title = { Text(chatTitle.ifBlank { "Approval Flow Demo" }) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Approval Flow Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The DeleteData function has requiresApproval = true. " +
                            "When the agent invokes it, you'll see an approval prompt " +
                            "before the function executes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Approval status indicator
            if (toolCallBatchState != ResolveKitToolCallBatchState.IDLE) {
                ApprovalStatusCard(
                    state = toolCallBatchState,
                    checklist = toolCallChecklist
                )
            }

            // Chat view
            ResolveKitChatView(
                runtime = runtime,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ApprovalStatusCard(
    state: ResolveKitToolCallBatchState,
    checklist: List<ToolCallChecklistItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                ResolveKitToolCallBatchState.AWAITING_APPROVAL ->
                    MaterialTheme.colorScheme.tertiaryContainer
                ResolveKitToolCallBatchState.APPROVED,
                ResolveKitToolCallBatchState.EXECUTING ->
                    MaterialTheme.colorScheme.secondaryContainer
                ResolveKitToolCallBatchState.DECLINED ->
                    MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⏳ Tool Call Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            checklist.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.functionName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = item.status.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (item.status) {
                            ResolveKitToolCallItemStatus.PENDING_APPROVAL -> Color.Gray
                            ResolveKitToolCallItemStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            ResolveKitToolCallItemStatus.COMPLETED -> Color.Green
                            ResolveKitToolCallItemStatus.FAILED,
                            ResolveKitToolCallItemStatus.CANCELLED -> Color.Red
                        }
                    )
                }
            }
        }
    }
}
