package app.resolvekit.sample

import android.app.Application
import app.resolvekit.core.JSONValue
import app.resolvekit.core.ResolveKitFunctionPack
import app.resolvekit.ui.ResolveKitConfiguration

/**
 * Application-level setup for the ResolveKit sample.
 *
 * Demonstrates creating a shared [ResolveKitConfiguration] that can be
 * reused across multiple screens and integration patterns.
 */
class SampleApplication : Application() {

    /**
     * Shared configuration used by all sample activities.
     *
     * In a real app you might inject this via Hilt/Koin or read from
     * encrypted SharedPreferences.
     */
    val configuration: ResolveKitConfiguration by lazy {
        ResolveKitConfiguration(
            baseUrl = BuildConfig.RESOLVEKIT_BASE_URL,
            apiKeyProvider = { BuildConfig.RESOLVEKIT_API_KEY.takeIf { it.isNotBlank() } },
            deviceIdProvider = {
                // In production, use a stable identifier from your auth system
                // or a securely persisted UUID.
                null // SDK will auto-generate and persist one
            },
            llmContextProvider = {
                mapOf(
                    "app_name" to JSONValue.String("ResolveKit Sample"),
                    "platform" to JSONValue.String("Android"),
                    "version" to JSONValue.String(BuildConfig.VERSION_NAME)
                )
            },
            functions = listOf(
                GetCurrentTime,
                AddNumbers,
                DeleteData,
                GetWeather,
                SearchNotes
            ),
            functionPacks = listOf(
                // Demonstrate function packs for modular tool grouping
                SampleFunctionPack
            )
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SampleApplication
            private set
    }
}

/**
 * A [ResolveKitFunctionPack] groups related tools together.
 * This pack bundles utility functions for the sample app.
 */
object SampleFunctionPack : ResolveKitFunctionPack {
    override val id = "sample_utilities"
    override val functions = listOf(EchoMessageAdapterHolder)
}

/**
 * Holder for the KSP-generated EchoMessage adapter.
 *
 * In a real project the KSP processor generates
 * `EchoMessageResolveKitAdapter` during build. We reference it
 * indirectly here so the sample compiles even before the first build.
 */
object EchoMessageAdapterHolder : AnyResolveKitFunction {
    // This will be replaced by the actual KSP-generated adapter at runtime.
    // See SampleFunctions.kt for the @ResolveKit annotation on EchoMessage.
    override val resolveKitName = "echo_message"
    override val resolveKitDescription = "Echoes back the provided message"
    override val resolveKitParametersSchema: Map<String, Any> = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(mapOf(
            "message" to JSONValue.Object(mapOf("type" to JSONValue.String("string")))
        )),
        "required" to JSONValue.Array(listOf(JSONValue.String("message")))
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(
        arguments: app.resolvekit.core.JSONObject,
        context: app.resolvekit.core.ResolveKitFunctionContext
    ): JSONValue {
        val message = app.resolvekit.core.TypeResolver.coerceToString(
            arguments["message"] ?: JSONValue.Null
        ) ?: ""
        return JSONValue.String(message)
    }
}
