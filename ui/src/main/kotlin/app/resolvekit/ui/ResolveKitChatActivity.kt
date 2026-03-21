package app.resolvekit.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ResolveKitChatActivity : ComponentActivity() {
    private val hostId: String by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_HOST_ID)
            ?: error("ResolveKitChatActivity requires a host id")
    }

    private val runtime by lazy(LazyThreadSafetyMode.NONE) {
        val factory = ResolveKitChatHostRegistry.resolve(hostId)
            ?: error("No ResolveKit runtime factory registered for id=$hostId")
        factory.create(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResolveKitChatView(runtime = runtime)
        }
    }

    override fun onDestroy() {
        runtime.stop()
        if (isFinishing && !isChangingConfigurations) {
            ResolveKitChatHostRegistry.unregister(hostId)
        }
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_HOST_ID = "resolvekit.host_id"

        fun createIntent(context: Context, factory: ResolveKitRuntimeFactory): Intent {
            val hostId = ResolveKitChatHostRegistry.register(factory)
            return Intent(context, ResolveKitChatActivity::class.java)
                .putExtra(EXTRA_HOST_ID, hostId)
        }

        fun createIntent(context: Context, configuration: ResolveKitConfiguration): Intent =
            createIntent(context) { appContext -> ResolveKitRuntime(configuration, appContext) }
    }
}
