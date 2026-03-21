package app.resolvekit.authoring

/**
 * Marks a class as a ResolveKit tool function.
 *
 * The KSP processor (in the :ksp module) reads this annotation at build time
 * and generates a companion object implementing [app.resolvekit.core.AnyResolveKitFunction].
 * The generated adapter is named `{ClassName}ResolveKitAdapter` and can be
 * passed directly to [app.resolvekit.ui.ResolveKitConfiguration.functions].
 *
 * Example:
 * ```kotlin
 * @ResolveKit(
 *     name = "send_message",
 *     description = "Send a text message to a contact",
 *     timeout = 15,
 *     requiresApproval = true
 * )
 * class SendMessage(val contactName: String, val messageText: String) : ResolveKitFunction {
 *     override suspend fun perform(): Boolean {
 *         // implementation
 *         return true
 *     }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ResolveKit(
    /** Unique tool name sent to the backend (snake_case recommended). */
    val name: String,
    /** Human-readable description shown to the LLM. */
    val description: String,
    /** Execution timeout in seconds. -1 means no timeout. */
    val timeout: Int = -1,
    /** Whether the user must approve this tool call before it executes. */
    val requiresApproval: Boolean = true
)
