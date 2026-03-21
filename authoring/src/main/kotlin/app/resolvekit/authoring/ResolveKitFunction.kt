package app.resolvekit.authoring

/**
 * Interface that @ResolveKit-annotated classes must implement.
 *
 * [perform] is called by the generated adapter when the LLM requests this tool.
 * Constructor parameters are the tool's input — they are automatically extracted
 * from the LLM-supplied JSON arguments by the generated adapter code.
 *
 * Supported constructor parameter types:
 * - String, Boolean
 * - Int, Long, Short, Byte, Double, Float
 * - Nullable variants of all the above (T?)
 *
 * Return type can be any of the above scalars, or null/Unit (returned as JSON null).
 */
interface ResolveKitFunction {
    suspend fun perform(): Any?
}
