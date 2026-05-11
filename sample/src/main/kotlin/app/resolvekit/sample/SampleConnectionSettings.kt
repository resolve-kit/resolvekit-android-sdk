package app.resolvekit.sample

import android.content.Context

private const val SETTINGS_PREFS = "resolvekit_sample_settings"
private const val KEY_BASE_URL = "base_url"
private const val KEY_API_KEY = "api_key"

private const val FALLBACK_BASE_URL = "https://agent.resolvekit.app"
const val MANAGED_BASE_URL = "https://agent.resolvekit.app"

data class SampleConnectionSettings(
    val baseUrl: String,
    val apiKey: String
) {
    fun normalizedBaseUrl(): String = baseUrl.trim().ifEmpty { FALLBACK_BASE_URL }
    fun normalizedApiKey(): String = apiKey.trim()
    fun hasApiKey(): Boolean = normalizedApiKey().isNotEmpty()
}

object SampleConnectionSettingsStore {
    fun load(context: Context): SampleConnectionSettings {
        val prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        val persistedBaseUrl = prefs.getString(KEY_BASE_URL, null)?.trim().orEmpty()
        val persistedApiKey = prefs.getString(KEY_API_KEY, null)?.trim().orEmpty()

        val defaultBaseUrl = BuildConfig.RESOLVEKIT_BASE_URL.trim()
        val defaultApiKey = BuildConfig.RESOLVEKIT_API_KEY.trim()

        return SampleConnectionSettings(
            baseUrl = persistedBaseUrl.ifEmpty { defaultBaseUrl.ifEmpty { FALLBACK_BASE_URL } },
            apiKey = persistedApiKey.ifEmpty { defaultApiKey }
        )
    }

    fun save(context: Context, settings: SampleConnectionSettings) {
        context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, settings.baseUrl.trim())
            .putString(KEY_API_KEY, settings.apiKey.trim())
            .apply()
    }
}
