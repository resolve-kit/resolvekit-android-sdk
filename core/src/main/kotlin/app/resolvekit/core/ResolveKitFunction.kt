package app.resolvekit.core

/**
 * Low-level interface for registering a tool function with ResolveKit.
 * Every function must supply its name, description, JSON Schema, and an
 * async [invoke] implementation.
 *
 * Use the @ResolveKit annotation (in the :authoring module) to generate a
 * conforming adapter automatically from a data class.
 */
interface AnyResolveKitFunction {
    val resolveKitName: String
    val resolveKitDescription: String
    val resolveKitParametersSchema: JSONObject
    val resolveKitTimeoutSeconds: Int?
    val resolveKitRequiresApproval: Boolean

    suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue
}

/**
 * Context passed to every function invocation, providing session and request identifiers.
 */
data class ResolveKitFunctionContext(
    val sessionID: String,
    val requestID: String?
)

/**
 * Groups related functions under a named pack. Packs can declare which platforms support them.
 */
interface ResolveKitFunctionPack {
    val packName: String
    val supportedPlatforms: List<ResolveKitPlatform>
    val functions: List<AnyResolveKitFunction>
}

enum class ResolveKitPlatform { ANDROID, IOS, WEB }
