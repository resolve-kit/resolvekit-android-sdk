package app.resolvekit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import app.resolvekit.ui.ResolveKitChatView
import app.resolvekit.ui.ResolveKitConfiguration
import app.resolvekit.ui.ResolveKitRuntime

/**
 * Sample activity demonstrating the ResolveKit Android SDK.
 *
 * Set RESOLVEKIT_API_KEY in your environment (or local.properties) before
 * running:
 *   RESOLVEKIT_API_KEY=iaa_... ./gradlew :sample:installDebug
 */
class MainActivity : ComponentActivity() {

    private val runtime by lazy {
        ResolveKitRuntime(
            configuration = ResolveKitConfiguration(
                apiKeyProvider = { BuildConfig.RESOLVEKIT_API_KEY.takeIf { it.isNotBlank() } },
                llmContextProvider = {
                    // Provide custom context to shape LLM responses
                    mapOf(
                        "app_name" to app.resolvekit.core.JSONValue.String("ResolveKit Sample"),
                        "platform" to app.resolvekit.core.JSONValue.String("Android")
                    )
                },
                functions = listOf(
                    GetCurrentTime,
                    AddNumbers,
                    DeleteData
                )
            ),
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ResolveKitChatView(runtime = runtime)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runtime.stop()
    }
}
