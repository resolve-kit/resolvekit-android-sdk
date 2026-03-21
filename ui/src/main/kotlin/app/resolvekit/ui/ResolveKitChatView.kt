package app.resolvekit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import app.resolvekit.ui.models.*
import kotlinx.coroutines.launch

/**
 * Full-screen chat interface driven by a [ResolveKitRuntime].
 *
 * Drop this into any Compose hierarchy. The runtime is started automatically
 * on first composition and stopped when the composable leaves the tree.
 *
 * ```kotlin
 * setContent {
 *     MaterialTheme {
 *         ResolveKitChatView(runtime = myRuntime)
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolveKitChatView(
    runtime: ResolveKitRuntime,
    modifier: Modifier = Modifier
) {
    val messages by runtime.messages.collectAsState()
    val connectionState by runtime.connectionState.collectAsState()
    val isTurnInProgress by runtime.isTurnInProgress.collectAsState()
    val toolCallChecklist by runtime.toolCallChecklist.collectAsState()
    val toolCallBatchState by runtime.toolCallBatchState.collectAsState()
    val chatTitle by runtime.chatTitle.collectAsState()
    val messagePlaceholder by runtime.messagePlaceholder.collectAsState()
    val appearanceMode by runtime.appearanceMode.collectAsState()
    val chatTheme by runtime.chatTheme.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val systemIsDark = isSystemInDarkTheme()
    val palette = remember(chatTheme, appearanceMode, systemIsDark) {
        ResolveKitTheme.resolvePaletteColors(
            theme = chatTheme,
            appearanceMode = appearanceMode,
            systemIsDark = systemIsDark
        )
    }

    // Auto-start on first composition
    LaunchedEffect(Unit) { runtime.start() }

    // Stop when leaving composition
    DisposableEffect(Unit) { onDispose { runtime.stop() } }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = palette.screenBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

                // ----- Toolbar -----
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.screenBackground,
                        titleContentColor = palette.titleText,
                        actionIconContentColor = palette.titleText
                    ),
                    title = {
                        Text(
                            text = chatTitle.ifBlank { "Assistant" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        TextButton(
                            onClick = { scope.launch { runtime.reloadWithNewSession() } },
                            enabled = connectionState != ResolveKitConnectionState.REGISTERING &&
                                    connectionState != ResolveKitConnectionState.CONNECTING,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = palette.titleText
                            )
                        ) {
                            Text("New chat")
                        }
                    }
                )

                // ----- Connection state banner -----
                when (connectionState) {
                    ResolveKitConnectionState.RECONNECTING ->
                        ConnectionBanner("Reconnecting…", palette)
                    ResolveKitConnectionState.RECONNECTED ->
                        ConnectionBanner("Reconnected", palette)
                    ResolveKitConnectionState.BLOCKED ->
                        ConnectionBanner("Chat unavailable", palette)
                    ResolveKitConnectionState.FAILED ->
                        ConnectionBanner("Connection failed", palette)
                    else -> Unit
                }

                // ----- Message list -----
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages, key = { it.id.toString() }) { message ->
                        ChatBubble(message = message, palette = palette)
                    }

                    if (isTurnInProgress && messages.lastOrNull()?.role != ChatMessageRole.ASSISTANT) {
                        item(key = "typing") { TypingIndicator(palette) }
                    }
                }

                if (toolCallBatchState == ResolveKitToolCallBatchState.AWAITING_APPROVAL &&
                    toolCallChecklist.isNotEmpty()
                ) {
                    ToolApprovalCard(
                        items = toolCallChecklist,
                        palette = palette,
                        onApprove = { scope.launch { runtime.approveToolCallBatch() } },
                        onDecline = { scope.launch { runtime.declineToolCallBatch() } }
                    )
                }

                if (toolCallBatchState == ResolveKitToolCallBatchState.EXECUTING) {
                    ToolApprovalCard(
                        items = toolCallChecklist,
                        palette = palette,
                        onApprove = {},
                        onDecline = {},
                        readOnly = true
                    )
                }

                var inputText by remember { mutableStateOf("") }
                val canSend = inputText.isNotBlank() && !isTurnInProgress &&
                        connectionState == ResolveKitConnectionState.ACTIVE

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = messagePlaceholder.ifBlank { "Message…" },
                                color = palette.composerPlaceholder
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.composerBackground,
                            unfocusedContainerColor = palette.composerBackground,
                            disabledContainerColor = palette.composerBackground.copy(alpha = 0.7f),
                            focusedTextColor = palette.composerText,
                            unfocusedTextColor = palette.composerText,
                            disabledTextColor = palette.composerText.copy(alpha = 0.7f),
                            focusedBorderColor = palette.toolCardBorder,
                            unfocusedBorderColor = palette.toolCardBorder.copy(alpha = 0.8f),
                            disabledBorderColor = palette.toolCardBorder.copy(alpha = 0.35f),
                            cursorColor = palette.composerText,
                            focusedPlaceholderColor = palette.composerPlaceholder,
                            unfocusedPlaceholderColor = palette.composerPlaceholder
                        ),
                        enabled = connectionState == ResolveKitConnectionState.ACTIVE && !isTurnInProgress,
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotEmpty()) {
                                inputText = ""
                                scope.launch { runtime.sendMessage(text) }
                            }
                        },
                        enabled = canSend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.userBubbleBackground,
                            contentColor = palette.userBubbleText,
                            disabledContainerColor = palette.userBubbleBackground.copy(alpha = 0.45f),
                            disabledContentColor = palette.userBubbleText.copy(alpha = 0.55f)
                        )
                    ) {
                        Text("Send")
                    }
                }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-composables
// -------------------------------------------------------------------------

@Composable
private fun ConnectionBanner(message: String, palette: ResolveKitPaletteColors) {
    Surface(color = palette.toolCardBackground, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = palette.statusText
        )
    }
}

@Composable
private fun ChatBubble(message: ResolveKitChatMessage, palette: ResolveKitPaletteColors) {
    val isUser = message.role == ChatMessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) palette.userBubbleBackground else palette.assistantBubbleBackground,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (isUser) palette.userBubbleText else palette.assistantBubbleText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypingIndicator(palette: ResolveKitPaletteColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = palette.loaderBubbleBackground
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 500, delayMillis = index * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = RoundedCornerShape(50),
                        color = when (index) {
                            0 -> palette.loaderDot1.copy(alpha = alpha)
                            1 -> palette.loaderDot2.copy(alpha = alpha)
                            else -> palette.loaderDot3.copy(alpha = alpha)
                        }
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun ToolApprovalCard(
    items: List<ToolCallChecklistItem>,
    palette: ResolveKitPaletteColors,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    readOnly: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.toolCardBackground),
        border = BorderStroke(1.dp, palette.toolCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (readOnly) "Executing…" else "Approve Actions",
                style = MaterialTheme.typography.titleSmall,
                color = palette.toolCardText
            )

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val statusIcon = when (item.status) {
                        ResolveKitToolCallItemStatus.COMPLETED -> "✓"
                        ResolveKitToolCallItemStatus.FAILED -> "✗"
                        ResolveKitToolCallItemStatus.RUNNING -> "⋯"
                        ResolveKitToolCallItemStatus.CANCELLED -> "—"
                        ResolveKitToolCallItemStatus.PENDING_APPROVAL -> "•"
                    }
                    Text(
                        text = statusIcon,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (item.status) {
                            ResolveKitToolCallItemStatus.COMPLETED -> palette.userBubbleBackground
                            ResolveKitToolCallItemStatus.FAILED -> Color(0xFFC62828)
                            else -> palette.toolCardStatus
                        }
                    )
                    Column {
                        Text(
                            text = item.functionName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.toolCardText
                        )
                        if (item.humanDescription.isNotBlank()) {
                            Text(
                                text = item.humanDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.toolCardStatus
                            )
                        }
                    }
                }
            }

            if (!readOnly) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.toolCardText)
                    ) { Text("Decline") }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.userBubbleBackground,
                            contentColor = palette.userBubbleText
                        )
                    ) { Text("Approve All") }
                }
            }
        }
    }
}
