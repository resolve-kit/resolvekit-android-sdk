package app.resolvekit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.resolvekit.ui.ResolveKitChatView
import app.resolvekit.ui.ResolveKitConfiguration
import app.resolvekit.ui.ResolveKitRuntime
import app.resolvekit.ui.models.ResolveKitConnectionState

/**
 * Demonstrates the Compose-first integration pattern.
 *
 * The ResolveKit runtime is created at the Activity level and the
 * ResolveKitChatView is embedded directly inside a Compose hierarchy.
 *
 * This is the recommended pattern for Compose-based apps.
 */
class ComposeChatActivity : ComponentActivity() {

    private val runtime by lazy {
        ResolveKitRuntime(
            configuration = (application as SampleApplication).configuration,
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ComposeChatScreen(
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
fun ComposeChatScreen(
    runtime: ResolveKitRuntime,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Observe runtime state
    val connectionState by runtime.connectionState.collectAsState()
    val chatTitle by runtime.chatTitle.collectAsState()
    val isTurnInProgress by runtime.isTurnInProgress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                title = {
                    Column {
                        Text(text = chatTitle.ifBlank { "ResolveKit Chat" })
                        Text(
                            text = when (connectionState) {
                                ResolveKitConnectionState.ACTIVE -> "Connected"
                                ResolveKitConnectionState.CONNECTING -> "Connecting..."
                                ResolveKitConnectionState.RECONNECTING -> "Reconnecting..."
                                else -> connectionState.name.lowercase()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (isTurnInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        ResolveKitChatView(
            runtime = runtime,
            modifier = modifier.padding(paddingValues)
        )
    }
}
