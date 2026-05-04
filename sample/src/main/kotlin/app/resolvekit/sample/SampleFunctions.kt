package app.resolvekit.sample

import app.resolvekit.authoring.ResolveKit
import app.resolvekit.authoring.ResolveKitFunction as RKFunction
import app.resolvekit.core.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===========================================================================
// KSP-Annotated Functions
//
// The KSP processor generates *ResolveKitAdapter classes in the build
// directory. These adapters implement AnyResolveKitFunction and can be
// passed directly into ResolveKitConfiguration.functions.
// ===========================================================================

/**
 * Example function using the @ResolveKit annotation + KSP codegen path.
 *
 * The KSP processor generates EchoMessageResolveKitAdapter in the build
 * directory, which handles parameter extraction and JSON serialization
 * automatically.
 */
@ResolveKit(
    name = "echo_message",
    description = "Echoes back the provided message",
    requiresApproval = false
)
class EchoMessage(val message: String) : RKFunction {
    override suspend fun perform(): Any = message
}

// ===========================================================================
// Manual AnyResolveKitFunction Implementations
//
// These objects implement AnyResolveKitFunction directly, giving full
// control over the JSON schema, parameter parsing, and return values.
// ===========================================================================

/**
 * Returns the current local time.
 *
 * Demonstrates: no parameters, simple string response.
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
 * Adds two numbers together.
 *
 * Demonstrates: numeric parameter coercion via TypeResolver,
 * required parameters in the JSON schema.
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
 * Deletes specified data — demonstrates the approval flow.
 *
 * With requiresApproval = true, the SDK shows an approval prompt
 * in the chat UI before executing this function. The user can
 * approve or deny the call.
 *
 * Try asking the agent: "Delete item with ID 'test-123'"
 */
object DeleteData : AnyResolveKitFunction {
    override val resolveKitName = "delete_data"
    override val resolveKitDescription = "Deletes specified data. This action requires user approval."
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
        return JSONValue.Object(mapOf(
            "deleted" to JSONValue.Bool(true),
            "item_id" to JSONValue.String(itemId)
        ))
    }
}

/**
 * Returns weather information for a given city.
 *
 * Demonstrates: single string parameter, simulated response.
 * In a production app, this would call a weather API.
 */
object GetWeather : AnyResolveKitFunction {
    override val resolveKitName = "get_weather"
    override val resolveKitDescription = "Returns the current weather for a specified city"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(mapOf(
            "city" to JSONValue.Object(mapOf(
                "type" to JSONValue.String("string"),
                "description" to JSONValue.String("The city name (e.g., 'London', 'Tokyo')")
            ))
        )),
        "required" to JSONValue.Array(listOf(JSONValue.String("city")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val city = TypeResolver.coerceToString(arguments["city"] ?: JSONValue.Null) ?: "Unknown"
        // Simulated weather data for demo purposes
        val conditions = listOf("Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Clear")
        val temp = (10..30).random()
        val condition = conditions.random()
        return JSONValue.Object(mapOf(
            "city" to JSONValue.String(city),
            "temperature" to JSONValue.Number(temp.toDouble()),
            "unit" to JSONValue.String("°C"),
            "condition" to JSONValue.String(condition),
            "note" to JSONValue.String("Simulated data — integrate a real weather API in production")
        ))
    }
}

/**
 * Searches notes by keyword.
 *
 * Demonstrates: optional parameter, list response type.
 */
object SearchNotes : AnyResolveKitFunction {
    override val resolveKitName = "search_notes"
    override val resolveKitDescription = "Searches notes by keyword and returns matching results"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(mapOf(
            "query" to JSONValue.Object(mapOf(
                "type" to JSONValue.String("string"),
                "description" to JSONValue.String("The search keyword")
            )),
            "limit" to JSONValue.Object(mapOf(
                "type" to JSONValue.String("integer"),
                "description" to JSONValue.String("Maximum number of results (default: 5)")
            ))
        )),
        "required" to JSONValue.Array(listOf(JSONValue.String("query")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val query = TypeResolver.coerceToString(arguments["query"] ?: JSONValue.Null) ?: ""
        val limit = (TypeResolver.coerceToInt(arguments["limit"] ?: JSONValue.Null) ?: 5).coerceIn(1, 20)

        // Simulated notes for demo purposes
        val sampleNotes = listOf(
            mapOf("title" to "Meeting Notes", "content" to "Discuss project timeline"),
            mapOf("title" to "Shopping List", "content" to "Milk, eggs, bread"),
            mapOf("title" to "Travel Plans", "content" to "Flight to Tokyo on March 15"),
            mapOf("title" to "Code Review", "content" to "Review pull request #42"),
            mapOf("title" to "Birthday Party", "content" to "Order cake for Saturday")
        )

        val results = sampleNotes
            .filter { it["title"]?.contains(query, ignoreCase = true) == true || it["content"]?.contains(query, ignoreCase = true) == true }
            .take(limit)

        return JSONValue.Array(results.map { note ->
            JSONValue.Object(note.mapValues { JSONValue.String(it.value) })
        })
    }
}
