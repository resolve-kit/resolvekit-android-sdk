package app.resolvekit.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe registry for [AnyResolveKitFunction] instances.
 *
 * Use [register] to add functions before starting a session, then [dispatch] to
 * execute them by name when the backend emits tool_call_request events.
 */
class ResolveKitRegistry {

    private val mutex = Mutex()
    private val functions = mutableMapOf<String, AnyResolveKitFunction>()

    /** Snapshot of definitions for all registered functions. */
    val definitions: List<ResolveKitDefinition>
        get() = synchronized(this) {
            functions.values.map { fn ->
                ResolveKitDefinition(
                    name = fn.resolveKitName,
                    description = fn.resolveKitDescription,
                    parametersSchema = fn.resolveKitParametersSchema,
                    timeoutSeconds = fn.resolveKitTimeoutSeconds,
                    requiresApproval = fn.resolveKitRequiresApproval
                )
            }
        }

    /** Register a single function. Throws [ResolveKitFunctionError.DuplicateFunctionName] if already present. */
    suspend fun register(function: AnyResolveKitFunction) = mutex.withLock {
        if (functions.containsKey(function.resolveKitName)) {
            throw ResolveKitFunctionError.DuplicateFunctionName(function.resolveKitName)
        }
        functions[function.resolveKitName] = function
    }

    /** Register multiple functions. Each must have a unique name. */
    suspend fun register(vararg fns: AnyResolveKitFunction) {
        fns.forEach { register(it) }
    }

    /** Register all functions from a [ResolveKitFunctionPack]. */
    suspend fun register(pack: ResolveKitFunctionPack) {
        pack.functions.forEach { register(it) }
    }

    /** Look up a function by name, or return null if not registered. */
    fun resolve(name: String): AnyResolveKitFunction? = functions[name]

    /**
     * Execute a function by name with the given [arguments].
     * Throws [ResolveKitFunctionError.UnknownFunction] if the function is not registered.
     */
    suspend fun dispatch(
        name: String,
        arguments: JSONObject,
        context: ResolveKitFunctionContext
    ): JSONValue {
        val fn = functions[name] ?: throw ResolveKitFunctionError.UnknownFunction(name)
        return fn.invoke(arguments, context)
    }

    /** Remove all registered functions (useful in tests). */
    suspend fun reset() = mutex.withLock { functions.clear() }

    /** Total number of registered functions. */
    val size: Int get() = functions.size
}
