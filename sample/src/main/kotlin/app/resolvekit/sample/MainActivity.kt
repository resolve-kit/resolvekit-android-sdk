package app.resolvekit.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SampleScreen {
    Configuration,
    Demo
}

private data class ToolGuide(
    val functionName: String,
    val prompt: String,
    val expectedResult: String
)

private val TOOL_GUIDES = listOf(
    ToolGuide(
        functionName = "set_demo_vibe",
        prompt = "Set the demo vibe to neon.",
        expectedResult = "Vibe and accent update on the app screen."
    ),
    ToolGuide(
        functionName = "launch_confetti",
        prompt = "Launch confetti with power 7.",
        expectedResult = "Confetti burst counter increases."
    ),
    ToolGuide(
        functionName = "rename_mascot",
        prompt = "Rename mascot to Laser Panda.",
        expectedResult = "Mascot name on the app screen changes."
    ),
    ToolGuide(
        functionName = "arm_lasers (approval required)",
        prompt = "Arm lasers.",
        expectedResult = "Approval sheet appears, then lasers state flips after approval."
    ),
    ToolGuide(
        functionName = "get_showcase_state",
        prompt = "Show current showcase state.",
        expectedResult = "Assistant returns current vibe/mascot/confetti/laser values."
    ),
    ToolGuide(
        functionName = "echo_message (KSP)",
        prompt = "Echo this exactly: ResolveKit is rad.",
        expectedResult = "Assistant returns the same text via generated adapter."
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleFlow(
                        initialSettings = SampleConnectionSettingsStore.load(this),
                        onSaveSettings = { settings -> SampleConnectionSettingsStore.save(this, settings) },
                        onOpenChat = { settings ->
                            SampleConnectionSettingsStore.save(this, settings)
                            startActivity(Intent(this, EmbeddedChatActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleFlow(
    initialSettings: SampleConnectionSettings,
    onSaveSettings: (SampleConnectionSettings) -> Unit,
    onOpenChat: (SampleConnectionSettings) -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(SampleScreen.Configuration.name) }
    var baseUrl by rememberSaveable { mutableStateOf(initialSettings.baseUrl) }
    var apiKey by rememberSaveable { mutableStateOf(initialSettings.apiKey) }
    var hasSavedOnce by rememberSaveable { mutableStateOf(false) }

    val activeScreen = SampleScreen.valueOf(screen)
    val settings = SampleConnectionSettings(baseUrl = baseUrl, apiKey = apiKey)

    when (activeScreen) {
        SampleScreen.Configuration -> ConfigurationScreen(
            settings = settings,
            hasSavedOnce = hasSavedOnce,
            onBaseUrlChanged = { baseUrl = it },
            onApiKeyChanged = { apiKey = it },
            onUseManaged = { baseUrl = MANAGED_BASE_URL },
            onContinue = {
                onSaveSettings(settings)
                hasSavedOnce = true
                screen = SampleScreen.Demo.name
            }
        )

        SampleScreen.Demo -> DemoScreen(
            settings = settings,
            onBackToConfiguration = { screen = SampleScreen.Configuration.name },
            onOpenChat = { onOpenChat(settings) }
        )
    }
}

@Composable
private fun ConfigurationScreen(
    settings: SampleConnectionSettings,
    hasSavedOnce: Boolean,
    onBaseUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onUseManaged: () -> Unit,
    onContinue: () -> Unit
) {
    val valid = settings.normalizedBaseUrl().isNotBlank() && settings.hasApiKey()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Step 1 of 2: Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        item {
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Connect sample app", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "You must provide host and API key before continuing.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = settings.baseUrl,
                        onValueChange = onBaseUrlChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Host URL") },
                        placeholder = { Text("https://agent.resolvekit.app") }
                    )

                    OutlinedTextField(
                        value = settings.apiKey,
                        onValueChange = onApiKeyChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("API Key") },
                        placeholder = { Text("rk_...") }
                    )

                    OutlinedButton(onClick = onUseManaged) {
                        Text("Use Managed Host")
                    }

                    if (!valid) {
                        Text(
                            "Configuration incomplete: both host and API key are required.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = onContinue,
                        enabled = valid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasSavedOnce) "Save & Continue" else "Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoScreen(
    settings: SampleConnectionSettings,
    onBackToConfiguration: () -> Unit,
    onOpenChat: () -> Unit
) {
    val appState by SampleShowcaseState.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Step 2 of 2: Capabilities",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        item {
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Connected settings", style = MaterialTheme.typography.titleMedium)
                    Text("Host: ${settings.normalizedBaseUrl()}", style = MaterialTheme.typography.bodySmall)
                    Text("API key: ${settings.normalizedApiKey().toDisplayToken()}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Live App State (changed by tool calls)", style = MaterialTheme.typography.titleMedium)
                    Text("Vibe: ${appState.vibe}", style = MaterialTheme.typography.bodySmall)
                    Text("Accent: ${appState.accent}", style = MaterialTheme.typography.bodySmall)
                    Text("Mascot: ${appState.mascot}", style = MaterialTheme.typography.bodySmall)
                    Text("Confetti bursts: ${appState.confettiBursts}", style = MaterialTheme.typography.bodySmall)
                    Text("Lasers armed: ${appState.lasersArmed}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Supported Functions", style = MaterialTheme.typography.titleMedium)
                    TOOL_GUIDES.forEach { guide ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(guide.functionName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Try: \"${guide.prompt}\"", style = MaterialTheme.typography.bodySmall)
                            Text("Expected: ${guide.expectedResult}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How To Test", style = MaterialTheme.typography.titleMedium)
                    Text("1. Tap Open Chat.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Send prompts from the Supported Functions list.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Return to this screen and verify Live App State changed.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onOpenChat, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Chat")
                    }
                    OutlinedButton(onClick = onBackToConfiguration, modifier = Modifier.fillMaxWidth()) {
                        Text("Back To Configuration")
                    }
                }
            }
        }
    }
}

private fun String.toDisplayToken(): String {
    if (isBlank()) return "(not set)"
    return if (length <= 8) "****" else "${take(4)}...${takeLast(4)}"
}
