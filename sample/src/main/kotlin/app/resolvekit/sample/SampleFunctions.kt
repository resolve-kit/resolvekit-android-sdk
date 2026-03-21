package app.resolvekit.sample

import app.resolvekit.authoring.ResolveKit
import app.resolvekit.authoring.ResolveKitFunction as RKFunction
import app.resolvekit.core.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Example function using the @ResolveKit annotation + KSP codegen path.
 * The KSP processor generates EchoMessageResolveKitAdapter in the build directory.
 */
@ResolveKit(name = "echo_message", description = "Echoes back the provided message", requiresApproval = false)
class EchoMessage(val message: String) : RKFunction {
    override suspend fun perform(): Any = message
}

/**
 * Example function: returns the current time.
 * Demonstrates the manual [AnyResolveKitFunction] implementation pattern.
 */
object GetCurrentTime : AnyResolveKitFunction {
    override val resolveKitName = "get_current_time"
    override val resolveKitDescription = "Returns the current local time as a formatted string"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(emptyMap())
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val time = SimpleDateFormat("HH:mm:ss z", Locale.getDefault()).format(Date())
        return JSONValue.String(time)
    }
}

/**
 * Example function: adds two numbers.
 * Demonstrates numeric parameter coercion via [app.resolvekit.core.TypeResolver].
 */
object AddNumbers : AnyResolveKitFunction {
    override val resolveKitName = "add_numbers"
    override val resolveKitDescription = "Adds two numbers and returns the result"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(mapOf(
            "a" to JSONValue.Object(mapOf("type" to JSONValue.String("number"))),
            "b" to JSONValue.Object(mapOf("type" to JSONValue.String("number")))
        )),
        "required" to JSONValue.Array(listOf(JSONValue.String("a"), JSONValue.String("b")))
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val a = TypeResolver.coerceToDouble(arguments["a"] ?: JSONValue.Null) ?: 0.0
        val b = TypeResolver.coerceToDouble(arguments["b"] ?: JSONValue.Null) ?: 0.0
        return JSONValue.Number(a + b)
    }
}

/**
 * Example function: shows how requiresApproval = true triggers the approval UI.
 */
object DeleteData : AnyResolveKitFunction {
    override val resolveKitName = "delete_data"
    override val resolveKitDescription = "Deletes specified data (requires approval)"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(mapOf(
            "item_id" to JSONValue.Object(mapOf("type" to JSONValue.String("string")))
        )),
        "required" to JSONValue.Array(listOf(JSONValue.String("item_id")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = true

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val itemId = TypeResolver.coerceToString(arguments["item_id"] ?: JSONValue.Null) ?: ""
        // Simulate deletion
        return JSONValue.Object(mapOf(
            "deleted" to JSONValue.Bool(true),
            "item_id" to JSONValue.String(itemId)
        ))
    }
}
