package app.resolvekit.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import app.resolvekit.core.*
import app.resolvekit.networking.ResolveKitAPIClient
import app.resolvekit.networking.ResolveKitEventStreamClient
import app.resolvekit.networking.models.*
import app.resolvekit.ui.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.math.min

/**
 * Central runtime managing the agent session lifecycle, event stream, tool
 * call approval flow, and observable UI state.
 *
 * Observe [StateFlow] properties from Compose with `collectAsState()`.
 */
class ResolveKitRuntime(
    val configuration: ResolveKitConfiguration,
    private val context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    private val registry = ResolveKitRegistry()

    // -------------------------------------------------------------------------
    // Public observable state
    // -------------------------------------------------------------------------

    private val _messages = MutableStateFlow<List<ResolveKitChatMessage>>(emptyList())
    val messages: StateFlow<List<ResolveKitChatMessage>> = _messages.asStateFlow()

    private val _connectionState = MutableStateFlow(ResolveKitConnectionState.IDLE)
    val connectionState: StateFlow<ResolveKitConnectionState> = _connectionState.asStateFlow()

    private val _isTurnInProgress = MutableStateFlow(false)
    val isTurnInProgress: StateFlow<Boolean> = _isTurnInProgress.asStateFlow()

    private val _toolCallChecklist = MutableStateFlow<List<ToolCallChecklistItem>>(emptyList())
    val toolCallChecklist: StateFlow<List<ToolCallChecklistItem>> = _toolCallChecklist.asStateFlow()

    private val _toolCallBatchState = MutableStateFlow(ResolveKitToolCallBatchState.IDLE)
    val toolCallBatchState: StateFlow<ResolveKitToolCallBatchState> = _toolCallBatchState.asStateFlow()

    private val _toolCallBatches = MutableStateFlow<List<ToolCallChecklistBatch>>(emptyList())
    val toolCallBatches: StateFlow<List<ToolCallChecklistBatch>> = _toolCallBatches.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _chatTitle = MutableStateFlow("")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _messagePlaceholder = MutableStateFlow("")
    val messagePlaceholder: StateFlow<String> = _messagePlaceholder.asStateFlow()

    private val _appearanceMode = MutableStateFlow(ResolveKitAppearanceMode.SYSTEM)
    val appearanceMode: StateFlow<ResolveKitAppearanceMode> = _appearanceMode.asStateFlow()

    private val _currentLocale = MutableStateFlow("en")
    val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

    private val _chatTheme = MutableStateFlow<ChatTheme?>(ResolveKitTheme.fallbackTheme)
    val chatTheme: StateFlow<ChatTheme?> = _chatTheme.asStateFlow()

    private val _executionLog = MutableStateFlow<List<String>>(emptyList())
    val executionLog: StateFlow<List<String>> = _executionLog.asStateFlow()

    // -------------------------------------------------------------------------
    // Internal session state
    // -------------------------------------------------------------------------

    private var sessionId: String? = null
    private var chatCapabilityToken: String? = null
    private var eventsUrl: String? = null
    private var lastEventCursor: String? = null
    private var currentTurnId: String? = null

    private var eventStreamJob: Job? = null
    private var heartbeatJob: Job? = null
    private var batchCoalesceJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val lastEventTimeMs = java.util.concurrent.atomic.AtomicLong(0L)
    private val batchMutex = Mutex()
    private val pendingToolCalls = mutableListOf<ToolCallChecklistItem>()
    private val pendingToolResultsMutex = Mutex()
    private val pendingToolResults = ResolveKitPendingToolResults()
    private var isFlushingPendingToolResults = false

    // -------------------------------------------------------------------------
    // Networking
    // -------------------------------------------------------------------------

    private val apiClient by lazy {
        ResolveKitAPIClient(
            baseUrl = configuration.baseUrl,
            apiKeyProvider = configuration.apiKeyProvider
        )
    }

    private val sseClient by lazy {
        ResolveKitEventStreamClient(
            baseUrl = configuration.baseUrl,
            apiKeyProvider = configuration.apiKeyProvider
        )
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initialize the runtime: register functions, check SDK compat, fetch theme,
     * create/reuse session, load history, and open event stream.
     */
    suspend fun start() {
        if (_connectionState.value != ResolveKitConnectionState.IDLE &&
            _connectionState.value != ResolveKitConnectionState.FAILED
        ) return

        _connectionState.value = ResolveKitConnectionState.REGISTERING
        _lastError.value = null

        try {
            // 1. Register all functions
            configuration.allFunctions.forEach {
                runCatching { registry.register(it) } // ignore duplicates on restart
            }

            // 2. Check SDK compatibility
            val compat = apiClient.checkCompat()
            val sdkMajor = 1
            if (!compat.supportedSdkMajorVersions.contains(sdkMajor)) {
                throw ResolveKitRuntimeError.UnsupportedSDK(
                    "SDK major version $sdkMajor not in supported list: ${compat.supportedSdkMajorVersions}"
                )
            }

            // 3. Fetch chat theme
            refreshChatTheme()

            // 4. Register functions with backend
            val definitions = registry.definitions
            if (definitions.isNotEmpty()) {
                val payloads = definitions.map { def ->
                    FunctionPayload(
                        name = def.name,
                        description = def.description,
                        parametersSchema = buildJsonObject {
                            def.parametersSchema.forEach { (k, v) ->
                                put(k, JSONValue.toJsonElement(v))
                            }
                        },
                        timeoutSeconds = def.timeoutSeconds,
                        requiresApproval = def.requiresApproval,
                        packName = def.packName,
                        source = def.source
                    )
                }
                runCatching { apiClient.registerFunctions(FunctionBulkRequest(payloads)) }
            }

            // 5. Create / reuse session
            _connectionState.value = ResolveKitConnectionState.CONNECTING
            val session = apiClient.createSession(buildSessionRequest())
            applySession(session, clearPendingResults = false)

            // 6. Load message history
            val history = runCatching {
                apiClient.getMessages(session.id, session.chatCapabilityToken)
            }.getOrDefault(emptyList())

            _messages.value = history.mapNotNull { msg ->
                val role = when (msg.role) {
                    "user" -> ChatMessageRole.USER
                    "assistant" -> ChatMessageRole.ASSISTANT
                    else -> return@mapNotNull null
                }
                ResolveKitChatMessage(role = role, text = msg.content ?: "")
            }

            // Add initial message only if history didn't already include it
            // (history endpoint already returns all messages including the initial one)
            session.initialMessage?.takeIf { !session.reusedActiveSession && _messages.value.isEmpty() }?.let { initialMsg ->
                _messages.value = listOf(ResolveKitChatMessage(
                    role = ChatMessageRole.ASSISTANT,
                    text = initialMsg
                ))
            }

            // 7. Open SSE stream
            _connectionState.value = ResolveKitConnectionState.ACTIVE
            startEventStream()
            registerNetworkCallback()
            flushPendingToolResults("start")

            log("Session started: ${session.id} (reused=${session.reusedActiveSession})")
        } catch (e: ResolveKitAPIClientError.ChatUnavailable) {
            _connectionState.value = ResolveKitConnectionState.BLOCKED
            _lastError.value = "Chat is unavailable"
            Log.e("ResolveKit", "Start failed: ChatUnavailable", e)
        } catch (e: ResolveKitRuntimeError.UnsupportedSDK) {
            _connectionState.value = ResolveKitConnectionState.BLOCKED
            _lastError.value = e.message
            Log.e("ResolveKit", "Start failed: UnsupportedSDK ${e.message}", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _connectionState.value = ResolveKitConnectionState.FAILED
            _lastError.value = e.message
            Log.e("ResolveKit", "Start failed: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    /** Stop the event stream and cancel all coroutines. */
    fun stop() {
        unregisterNetworkCallback()
        eventStreamJob?.cancel()
        heartbeatJob?.cancel()
        batchCoalesceJob?.cancel()
        _connectionState.value = ResolveKitConnectionState.IDLE
    }

    /** Clear history, create a new session, and reconnect. */
    suspend fun reloadWithNewSession() {
        stop()
        _messages.value = emptyList()
        _toolCallBatches.value = emptyList()
        _toolCallChecklist.value = emptyList()
        _toolCallBatchState.value = ResolveKitToolCallBatchState.IDLE
        _isTurnInProgress.value = false
        sessionId = null
        chatCapabilityToken = null
        eventsUrl = null
        lastEventCursor = null
        currentTurnId = null
        pendingToolResults.clear()
        _connectionState.value = ResolveKitConnectionState.IDLE
        start()
    }

    /** Push updated llm_context, available functions, or locale to the backend without restarting. */
    suspend fun refreshSessionContext() {
        val sid = sessionId ?: return
        val tok = chatCapabilityToken ?: return
        val names = configuration.availableFunctionNamesProvider?.invoke()
            ?: registry.definitions.map { it.name }
        runCatching {
            apiClient.patchSessionContext(
                sid, tok,
                SessionContextPatchRequest(
                    llmContext = buildLlmContext(),
                    availableFunctionNames = names,
                    locale = configuration.localeProvider?.invoke()
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // User actions
    // -------------------------------------------------------------------------

    /** Submit a user message. Appends optimistically to [messages] immediately. */
    suspend fun sendMessage(text: String) {
        val sid = sessionId ?: return
        val tok = chatCapabilityToken ?: return
        if (_isTurnInProgress.value) return

        _messages.value = _messages.value + ResolveKitChatMessage(
            role = ChatMessageRole.USER,
            text = text
        )
        _isTurnInProgress.value = true

        val requestId = UUID.randomUUID().toString()
        log("Sending message requestId=$requestId")
        runCatching {
            apiClient.sendMessage(sid, tok, MessageRequest(text = text, requestId = requestId))
        }.onSuccess {
            log("Message accepted: turnId=${it.turnId}")
        }.onFailure {
            log("Send failed: ${it.message}")
            _isTurnInProgress.value = false
            _lastError.value = it.message
        }
    }

    /** Approve all tools in the current batch. Executes them and submits results. */
    suspend fun approveToolCallBatch() {
        val items = _toolCallChecklist.value.toList()
        if (items.isEmpty()) return

        _toolCallBatchState.value = ResolveKitToolCallBatchState.APPROVED
        _toolCallBatchState.value = ResolveKitToolCallBatchState.EXECUTING

        items.forEach { item ->
            updateItemStatus(item.id, ResolveKitToolCallItemStatus.RUNNING)
            try {
                val argMap = parseArguments(item.rawArguments)
                val ctx = ResolveKitFunctionContext(sessionId ?: "", currentTurnId)
                val result = registry.dispatch(item.functionName, argMap, ctx)
                updateItemStatus(item.id, ResolveKitToolCallItemStatus.COMPLETED)
                submitToolResult(item, status = "success", result = JSONValue.toJsonElement(result))
                log("Tool '${item.functionName}' completed")
            } catch (e: Exception) {
                updateItemStatus(item.id, ResolveKitToolCallItemStatus.FAILED)
                submitToolResult(item, status = "error", errorMsg = e.message)
                log("Tool '${item.functionName}' failed: ${e.message}")
            }
        }

        finishBatch(items, approved = true)
    }

    /** Decline all tools in the current batch. Submits error results for each. */
    suspend fun declineToolCallBatch() {
        val items = _toolCallChecklist.value.toList()
        _toolCallBatchState.value = ResolveKitToolCallBatchState.DECLINED

        items.forEach { item ->
            updateItemStatus(item.id, ResolveKitToolCallItemStatus.CANCELLED)
            submitToolResult(item, status = "error", errorMsg = "User declined")
        }

        finishBatch(items, approved = false)
        log("Tool batch declined by user")
    }

    /** Override the color scheme. */
    fun setAppearance(mode: ResolveKitAppearanceMode) {
        _appearanceMode.value = mode
    }

    fun resolvedPalette(systemIsDark: Boolean): ChatThemePalette =
        ResolveKitTheme.resolvePalette(_chatTheme.value, _appearanceMode.value, systemIsDark)

    /** Override the chat language. Fetches updated UI strings from the backend. */
    suspend fun setLocale(locale: String?) {
        _currentLocale.value = locale ?: "en"
        val sid = sessionId ?: return
        val tok = chatCapabilityToken ?: return
        runCatching {
            val l10n = apiClient.getLocalization(sid, tok)
            _chatTitle.value = l10n.chatTitle
            _messagePlaceholder.value = l10n.messagePlaceholder
        }
    }

    private suspend fun refreshChatTheme() {
        try {
            _chatTheme.value = apiClient.getChatTheme()
        } catch (e: ResolveKitAPIClientError.ServerError) {
            if (e.statusCode != 404) {
                Log.w("ResolveKit", "Failed to fetch chat theme: ${e.message}", e)
            }
            _chatTheme.value = ResolveKitTheme.fallbackTheme
        } catch (e: Exception) {
            Log.w("ResolveKit", "Failed to fetch chat theme: ${e.message}", e)
            _chatTheme.value = ResolveKitTheme.fallbackTheme
        }
    }

    // -------------------------------------------------------------------------
    // Event stream
    // -------------------------------------------------------------------------

    private fun startEventStream() {
        startHeartbeatWatchdog()
        eventStreamJob?.cancel()
        eventStreamJob = scope.launch {
            var backoffMs = 1_000L

            while (isActive) {
                try {
                    val url = eventsUrl ?: return@launch
                    val tok = chatCapabilityToken ?: return@launch
                    sseClient.stream(url, tok, lastEventCursor).collect { event ->
                        backoffMs = 1_000L
                        if (_connectionState.value == ResolveKitConnectionState.RECONNECTING) {
                            _connectionState.value = ResolveKitConnectionState.RECONNECTED
                            flushPendingToolResults("stream-reconnected")
                        }
                        handleEvent(event)
                    }
                    // Stream closed cleanly — reconnect after a brief pause
                    // (backend may close connection after each turn)
                    log("Stream closed cleanly, reconnecting in 500ms (cursor=$lastEventCursor)")
                    delay(500L)
                } catch (e: ResolveKitAPIClientError.ChatUnavailable) {
                    _connectionState.value = ResolveKitConnectionState.BLOCKED
                    _lastError.value = e.message
                    log("Stream blocked: ${e.message}")
                    break
                } catch (e: ResolveKitAPIClientError.MissingAPIKey) {
                    log("Stream auth failed, refreshing active session")
                    _connectionState.value = ResolveKitConnectionState.RECONNECTING
                    if (!refreshSessionAfterAuthFailure("event-stream")) {
                        _connectionState.value = ResolveKitConnectionState.FAILED
                        _lastError.value = e.message
                        break
                    }
                    backoffMs = 1_000L
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    log("Stream error: ${e.message}. Reconnecting in ${backoffMs}ms")
                    _connectionState.value = ResolveKitConnectionState.RECONNECTING
                    delay(backoffMs)
                    backoffMs = min(backoffMs * 2, 30_000L)
                    _connectionState.value = ResolveKitConnectionState.RECONNECTED
                }
            }
        }
    }

    private suspend fun handleEvent(event: app.resolvekit.networking.models.ResolveKitEvent) {
        Log.d("ResolveKit", "handleEvent: ${event::class.simpleName}")
        lastEventTimeMs.set(System.currentTimeMillis())
        when (event) {
            is app.resolvekit.networking.models.ResolveKitEvent.TextDelta -> {
                lastEventCursor = event.eventId
                saveEventCursor(event.eventId, sessionId ?: return)
                _isTurnInProgress.value = true
                val msgs = _messages.value.toMutableList()
                val last = msgs.lastOrNull()
                if (last?.role == ChatMessageRole.ASSISTANT) {
                    msgs[msgs.lastIndex] = last.copy(text = event.accumulated)
                } else {
                    msgs += ResolveKitChatMessage(
                        role = ChatMessageRole.ASSISTANT,
                        text = event.accumulated
                    )
                }
                _messages.value = msgs
            }

            is app.resolvekit.networking.models.ResolveKitEvent.ToolCallRequest -> {
                lastEventCursor = event.eventId
                saveEventCursor(event.eventId, sessionId ?: return)
                currentTurnId = event.turnId
                val item = ToolCallChecklistItem(
                    id = event.payload.callId,
                    functionName = event.payload.functionName,
                    humanDescription = event.payload.humanDescription,
                    timeoutSeconds = event.payload.timeoutSeconds,
                    requiresApproval = event.payload.requiresApproval,
                    rawArguments = event.payload.arguments.toString()
                )
                batchMutex.withLock { pendingToolCalls += item }
                // Coalesce rapid tool_call_request events into a single batch (~250ms window)
                batchCoalesceJob?.cancel()
                batchCoalesceJob = scope.launch {
                    delay(250)
                    flushPendingBatch()
                }
            }

            is app.resolvekit.networking.models.ResolveKitEvent.TurnComplete -> {
                lastEventCursor = event.eventId
                saveEventCursor(event.eventId, sessionId ?: return)
                _isTurnInProgress.value = false
                currentTurnId = null
                val msgs = _messages.value.toMutableList()
                if (msgs.lastOrNull()?.role == ChatMessageRole.ASSISTANT) {
                    // Finalise an in-progress streaming message
                    msgs[msgs.lastIndex] = msgs.last().copy(text = event.fullText)
                } else if (event.fullText.isNotEmpty()) {
                    // No text_delta events were received — backend delivered full text directly
                    msgs += ResolveKitChatMessage(role = ChatMessageRole.ASSISTANT, text = event.fullText)
                }
                _messages.value = msgs
                log("Turn complete: ${event.fullText.take(60)}")
            }

            is app.resolvekit.networking.models.ResolveKitEvent.ServerError -> {
                lastEventCursor = event.eventId
                saveEventCursor(event.eventId, sessionId ?: return)
                log("Server error [${event.code}]: ${event.message} (recoverable=${event.recoverable})")
                if (!event.recoverable) {
                    _connectionState.value = ResolveKitConnectionState.FAILED
                    _lastError.value = event.message
                }
            }

            is app.resolvekit.networking.models.ResolveKitEvent.Unknown -> {
                lastEventCursor = event.eventId
                saveEventCursor(event.eventId, sessionId ?: return)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tool call batch management
    // -------------------------------------------------------------------------

    private suspend fun flushPendingBatch() {
        val items = batchMutex.withLock {
            val copy = pendingToolCalls.toList()
            pendingToolCalls.clear()
            copy
        }
        if (items.isEmpty()) return

        val autoItems = items.filter { !it.requiresApproval }
        val approvalItems = items.filter { it.requiresApproval }

        if (autoItems.isNotEmpty()) {
            log("Auto-executing ${autoItems.size} tool(s) (requiresApproval=false): ${autoItems.map { it.functionName }}")
            autoItems.forEach { item ->
                scope.launch {
                    try {
                        val argMap = parseArguments(item.rawArguments)
                        val ctx = ResolveKitFunctionContext(sessionId ?: "", currentTurnId)
                        val result = registry.dispatch(item.functionName, argMap, ctx)
                        submitToolResult(item, status = "success", result = JSONValue.toJsonElement(result))
                        log("Auto-approved '${item.functionName}' completed")
                    } catch (e: Exception) {
                        submitToolResult(item, status = "error", errorMsg = e.message)
                        log("Auto-approved '${item.functionName}' failed: ${e.message}")
                    }
                }
            }
        }

        if (approvalItems.isNotEmpty()) {
            val batch = ToolCallChecklistBatch(items = approvalItems)
            _toolCallChecklist.value = approvalItems
            _toolCallBatchState.value = ResolveKitToolCallBatchState.AWAITING_APPROVAL
            _toolCallBatches.value = _toolCallBatches.value + batch
            log("Awaiting approval for ${approvalItems.size} tool(s): ${approvalItems.map { it.functionName }}")
        }
    }

    private fun updateItemStatus(callId: String, status: ResolveKitToolCallItemStatus) {
        _toolCallChecklist.value = _toolCallChecklist.value.map { item ->
            if (item.id == callId) item.copy(status = status) else item
        }
    }

    private fun finishBatch(items: List<ToolCallChecklistItem>, approved: Boolean) {
        _toolCallBatches.value = _toolCallBatches.value.map { batch ->
            if (batch.items.any { it.id == items.firstOrNull()?.id }) {
                batch.copy(
                    state = if (approved)
                        ResolveKitToolCallBatchState.FINISHED
                    else
                        ResolveKitToolCallBatchState.DECLINED
                )
            } else batch
        }
        _toolCallBatchState.value = ResolveKitToolCallBatchState.FINISHED
        _toolCallChecklist.value = emptyList()
    }

    private fun submitToolResult(
        item: ToolCallChecklistItem,
        status: String,
        result: JsonElement? = null,
        errorMsg: String? = null
    ) {
        scope.launch {
            pendingToolResultsMutex.withLock {
                pendingToolResults.enqueue(
                    PendingToolResult(
                        callId = item.id,
                        turnId = currentTurnId ?: "",
                        status = status,
                        result = result,
                        error = errorMsg
                    )
                )
            }
            flushPendingToolResults("tool-result")
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun getOrCreateDeviceId(): String {
        configuration.deviceIdProvider?.invoke()?.let { return it }
        val prefs: SharedPreferences = context?.getSharedPreferences("resolvekit", Context.MODE_PRIVATE)
            ?: return UUID.randomUUID().toString()
        val key = "device_id.${configuration.baseUrl.hashCode()}"
        return prefs.getString(key, null) ?: UUID.randomUUID().toString().also { id ->
            prefs.edit().putString(key, id).apply()
        }
    }

    private fun buildSessionRequest(): SessionCreateRequest {
        val fns = configuration.availableFunctionNamesProvider?.invoke()
            ?: registry.definitions.map { it.name }
        return SessionCreateRequest(
            deviceId = getOrCreateDeviceId(),
            client = ClientContext(
                osVersion = android.os.Build.VERSION.RELEASE,
                appVersion = getAppVersion(),
                appBuild = getAppBuild()
            ),
            llmContext = buildLlmContext(),
            availableFunctionNames = fns.ifEmpty { null },
            locale = configuration.localeProvider?.invoke(),
            preferredLocales = configuration.preferredLocalesProvider?.invoke()
        )
    }

    private fun buildLlmContext(): Map<String, JsonElement> =
        configuration.llmContextProvider().mapValues { (_, value) -> JSONValue.toJsonElement(value) }

    private fun getAppVersion(): String = runCatching {
        context?.packageManager
            ?.getPackageInfo(context.packageName, 0)
            ?.versionName ?: "unknown"
    }.getOrDefault("unknown")

    private fun getAppBuild(): String = runCatching {
        @Suppress("DEPRECATION")
        context?.packageManager
            ?.getPackageInfo(context.packageName, 0)
            ?.versionCode?.toString() ?: "0"
    }.getOrDefault("0")

    private fun loadEventCursor(sessionId: String): String? =
        context?.getSharedPreferences("resolvekit", Context.MODE_PRIVATE)
            ?.getString("event_cursor.$sessionId", null)

    private fun saveEventCursor(cursor: String, sessionId: String) {
        context?.getSharedPreferences("resolvekit", Context.MODE_PRIVATE)
            ?.edit()?.putString("event_cursor.$sessionId", cursor)?.apply()
    }

    private fun registerNetworkCallback() {
        val ctx = context ?: return
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (_connectionState.value == ResolveKitConnectionState.RECONNECTING ||
                    _connectionState.value == ResolveKitConnectionState.ACTIVE) {
                    log("Network available — immediate reconnect")
                    startEventStream()
                }
            }
        }
        runCatching { cm.registerNetworkCallback(req, networkCallback!!) }
    }

    private fun unregisterNetworkCallback() {
        val ctx = context ?: return
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        networkCallback?.let { runCatching { cm.unregisterNetworkCallback(it) }; networkCallback = null }
    }

    private fun startHeartbeatWatchdog() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(5_000L)
                val last = lastEventTimeMs.get()
                if (last > 0 &&
                    System.currentTimeMillis() - last > 25_000L &&
                    _connectionState.value == ResolveKitConnectionState.ACTIVE) {
                    log("Heartbeat: stream stale >25s, reconnecting")
                    lastEventTimeMs.set(System.currentTimeMillis())
                    startEventStream()
                }
            }
        }
    }

    private fun parseArguments(rawJson: String): JSONObject =
        runCatching {
            val element = Json.parseToJsonElement(rawJson)
            if (element is JsonObject) {
                element.mapValues { (_, v) -> JSONValue.fromJsonElement(v) }
            } else emptyMap()
        }.getOrDefault(emptyMap())

    private fun log(message: String) {
        Log.d("ResolveKit", message)
        _executionLog.value = (_executionLog.value + message).takeLast(200)
    }

    private fun applySession(session: SessionResponse, clearPendingResults: Boolean) {
        val previousSessionId = sessionId
        sessionId = session.id
        if (previousSessionId != null && previousSessionId != session.id && clearPendingResults) {
            pendingToolResults.clear()
            currentTurnId = null
        }
        lastEventCursor = loadEventCursor(session.id)
        chatCapabilityToken = session.chatCapabilityToken
        eventsUrl = session.eventsUrl
        _chatTitle.value = session.chatTitle ?: _chatTitle.value
        _messagePlaceholder.value = session.messagePlaceholder ?: _messagePlaceholder.value
        session.locale?.let { _currentLocale.value = it }
    }

    private suspend fun refreshSessionAfterAuthFailure(reason: String): Boolean {
        return runCatching {
            val session = apiClient.createSession(buildSessionRequest())
            applySession(session, clearPendingResults = true)
            log("Refreshed active session after $reason: ${session.id}")
            true
        }.getOrElse {
            log("Session refresh failed after $reason: ${it.message}")
            false
        }
    }

    private suspend fun flushPendingToolResults(reason: String) {
        if (isFlushingPendingToolResults) return

        isFlushingPendingToolResults = true
        var shouldRetryAfterRefresh = false
        try {
            val submitted = linkedSetOf<String>()
            val snapshot = pendingToolResultsMutex.withLock { pendingToolResults.snapshot() }
            if (snapshot.isEmpty()) return

            for (payload in snapshot) {
                val sid = sessionId ?: break
                val tok = chatCapabilityToken ?: break
                val submission = runCatching {
                    apiClient.submitToolResult(
                        sid,
                        tok,
                        ToolResultRequest(
                            turnId = payload.turnId,
                            idempotencyKey = payload.idempotencyKey,
                            callId = payload.callId,
                            status = payload.status,
                            result = payload.result,
                            error = payload.error
                        )
                    )
                }

                if (submission.isSuccess) {
                    submitted += payload.callId
                    continue
                }

                val error = submission.exceptionOrNull()
                if (!shouldRetryAfterRefresh && error is ResolveKitAPIClientError.MissingAPIKey) {
                    shouldRetryAfterRefresh = refreshSessionAfterAuthFailure("tool-result")
                }
                log("Queued tool result retained for ${payload.callId} ($reason): ${error?.message}")
                break
            }

            if (submitted.isNotEmpty()) {
                pendingToolResultsMutex.withLock { pendingToolResults.removeAll(submitted) }
                log("Flushed ${submitted.size} queued tool result(s) ($reason)")
            }
        } finally {
            isFlushingPendingToolResults = false
        }
        if (shouldRetryAfterRefresh) {
            flushPendingToolResults("$reason-refreshed")
        }
    }
}
