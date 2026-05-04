package app.resolvekit.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Launcher activity that immediately redirects to the Dashboard.
 *
 * Kept as the MAIN/LAUNCHER entry point for backward compatibility
 * with existing run configurations and launch intents.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
