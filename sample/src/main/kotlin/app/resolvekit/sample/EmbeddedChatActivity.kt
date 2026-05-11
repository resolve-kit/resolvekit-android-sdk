package app.resolvekit.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import app.resolvekit.ui.ResolveKitChatView
import app.resolvekit.ui.ResolveKitRuntime

class EmbeddedChatActivity : ComponentActivity() {

    private val runtime by lazy {
        ResolveKitRuntime(
            configuration = SampleRuntimeFactory.createConfiguration(this),
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
        runtime.stop()
        super.onDestroy()
    }
}
