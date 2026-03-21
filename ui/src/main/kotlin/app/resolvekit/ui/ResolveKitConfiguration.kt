package app.resolvekit.ui

import app.resolvekit.core.AnyResolveKitFunction
import app.resolvekit.core.JSONObject
import app.resolvekit.core.ResolveKitFunctionPack

/**
 * Immutable configuration for [ResolveKitRuntime].
 *
 * All providers are called lazily at runtime, allowing dynamic values
 * (e.g. freshly-fetched API keys, current user context) without restarting
 * the runtime.
 *
 * @param baseUrl              Backend base URL. Defaults to the production endpoint.
 * @param apiKeyProvider       Returns the bearer token for API requests. Called on every request.
 * @param deviceIdProvider     Optional stable device identifier. Auto-generated UUID if null.
 * @param llmContextProvider   Custom JSON context forwarded to the LLM (user prefs, location, etc.).
 * @param availableFunctionNamesProvider  Optional allowlist — limits which registered functions
 *                             the backend can call in a session. Null means all are available.
 * @param localeProvider       BCP 47 locale override (e.g. "en", "lt", "fr-CA"). Uses system
 *                             locale when null.
 * @param preferredLocalesProvider  Ordered fallback list when [localeProvider] is null.
 * @param functions            Inline tool functions registered with this configuration.
 * @param functionPacks        Grouped tool modules; all functions in each pack are registered.
 */
data class ResolveKitConfiguration(
    val baseUrl: String = "https://agent.resolvekit.app",
    val apiKeyProvider: () -> String?,
    val deviceIdProvider: (() -> String?)? = null,
    val llmContextProvider: () -> JSONObject = { emptyMap() },
    val availableFunctionNamesProvider: (() -> List<String>)? = null,
    val localeProvider: (() -> String?)? = null,
    val preferredLocalesProvider: (() -> List<String>)? = null,
    val functions: List<AnyResolveKitFunction> = emptyList(),
    val functionPacks: List<ResolveKitFunctionPack> = emptyList()
) {
    /** All functions from both inline [functions] and [functionPacks]. */
    val allFunctions: List<AnyResolveKitFunction>
        get() = functions + functionPacks.flatMap { it.functions }
}
