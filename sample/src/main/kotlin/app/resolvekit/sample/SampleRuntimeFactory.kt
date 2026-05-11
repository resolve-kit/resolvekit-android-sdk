package app.resolvekit.sample

import android.content.Context
import app.resolvekit.core.JSONValue
import app.resolvekit.ui.ResolveKitConfiguration

object SampleRuntimeFactory {
    val availableFunctions: List<String>
        get() = listOf(
            "set_demo_vibe",
            "launch_confetti",
            "rename_mascot",
            "arm_lasers",
            "get_showcase_state",
            "echo_message"
        )

    fun createConfiguration(context: Context): ResolveKitConfiguration {
        val settings = SampleConnectionSettingsStore.load(context)
        val resolvedApiKey = settings.normalizedApiKey()
        val resolvedBaseUrl = settings.normalizedBaseUrl()

        return ResolveKitConfiguration(
            baseUrl = resolvedBaseUrl,
            apiKeyProvider = { resolvedApiKey.takeIf { it.isNotEmpty() } },
            llmContextProvider = {
                mapOf(
                    "app_name" to JSONValue.String("ResolveKit Sample"),
                    "platform" to JSONValue.String("Android"),
                    "demo_goal" to JSONValue.String("Call tools to change visible app state")
                )
            },
            availableFunctionNamesProvider = { availableFunctions },
            functionPacks = listOf(SampleUtilityFunctionPack)
        )
    }
}
