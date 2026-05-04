package app.resolvekit.sample

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import app.resolvekit.ui.ResolveKitConfiguration
import app.resolvekit.ui.ResolveKitChatFragment

/**
 * Demonstrates embedding ResolveKitChatFragment inside a View-based Activity.
 *
 * This pattern is ideal for apps that haven't migrated to Compose yet or
 * need to integrate the chat into an existing Fragment-based navigation graph.
 */
class FragmentHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.google.android.material.R.style.Theme_Material3_Light_NoActionBar)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fragment_host)

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        // Embed the chat fragment only on first creation
        if (savedInstanceState == null) {
            val configuration = ResolveKitConfiguration(
                baseUrl = BuildConfig.RESOLVEKIT_BASE_URL,
                apiKeyProvider = { BuildConfig.RESOLVEKIT_API_KEY.takeIf { it.isNotBlank() } },
                functions = listOf(
                    GetCurrentTime,
                    AddNumbers,
                    GetWeather,
                    SearchNotes
                )
            )

            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container,
                    ResolveKitChatFragment.newInstance(configuration)
                )
            }
        }
    }
}
