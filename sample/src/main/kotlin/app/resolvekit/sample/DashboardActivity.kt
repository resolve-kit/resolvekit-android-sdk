package app.resolvekit.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dashboard activity showcasing all integration patterns available in the
 * ResolveKit Android SDK.
 *
 * This is the recommended entry point for exploring the sample app.
 */
class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(onNavigate = ::navigateTo)
                }
            }
        }
    }

    private fun navigateTo(destination: DashboardDestination) {
        val intent = when (destination) {
            DashboardDestination.ComposeChat ->
                Intent(this, ComposeChatActivity::class.java)
            DashboardDestination.ActivityLaunch ->
                ResolveKitChatActivity.createIntent(
                    context = this,
                    configuration = (application as SampleApplication).configuration
                )
            DashboardDestination.FragmentHost ->
                Intent(this, FragmentHostActivity::class.java)
            DashboardDestination.ApprovalDemo ->
                Intent(this, ApprovalDemoActivity::class.java)
        }
        startActivity(intent)
    }
}

enum class DashboardDestination(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    ComposeChat(
        title = "Compose Chat",
        description = "Embed ResolveKitChatView directly in a Compose UI",
        icon = Icons.Default.Chat
    ),
    ActivityLaunch(
        title = "Full-Screen Activity",
        description = "Launch ResolveKitChatActivity as a standalone screen",
        icon = Icons.Default.OpenInNew
    ),
    FragmentHost(
        title = "Fragment Embedding",
        description = "Embed ResolveKitChatFragment inside a View-based layout",
        icon = Icons.Default.Dashboard
    ),
    ApprovalDemo(
        title = "Approval Flow Demo",
        description = "See the tool-call approval UI with requiresApproval functions",
        icon = Icons.Default.Security
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (DashboardDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = DashboardDestination.entries.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ResolveKit Sample") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Integration Patterns",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose a demo below to see different ways to integrate the ResolveKit SDK into your Android app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            destinations.forEach { dest ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate(dest) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dest.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = dest.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "⚙️ Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All demos share a single ResolveKitConfiguration from SampleApplication. " +
                            "Set your API key via local.properties or the RESOLVEKIT_API_KEY environment variable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
