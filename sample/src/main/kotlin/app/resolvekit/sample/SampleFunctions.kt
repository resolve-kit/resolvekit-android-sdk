package app.resolvekit.sample

import app.resolvekit.authoring.ResolveKit
import app.resolvekit.authoring.ResolveKitFunction as RKFunction
import app.resolvekit.core.AnyResolveKitFunction
import app.resolvekit.core.JSONObject
import app.resolvekit.core.JSONValue
import app.resolvekit.core.ResolveKitFunctionContext
import app.resolvekit.core.ResolveKitFunctionPack
import app.resolvekit.core.ResolveKitPlatform
import app.resolvekit.core.TypeResolver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ResolveKit(
    name = "echo_message",
    description = "Returns the same message back to the user",
    requiresApproval = false
)
class EchoMessage(val message: String) : RKFunction {
    override suspend fun perform(): Any = message
}

object SetDemoVibe : AnyResolveKitFunction {
    override val resolveKitName = "set_demo_vibe"
    override val resolveKitDescription = "Sets app vibe preset: chill, neon, or chaos"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf("vibe" to JSONValue.Object(mapOf("type" to JSONValue.String("string"))))
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("vibe")))
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val vibe = TypeResolver.coerceToString(arguments["vibe"] ?: JSONValue.Null) ?: "chill"
        return SampleShowcaseState.asJson(SampleShowcaseState.setVibe(vibe))
    }
}

object LaunchConfetti : AnyResolveKitFunction {
    override val resolveKitName = "launch_confetti"
    override val resolveKitDescription = "Adds confetti bursts to the demo counter"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf("power" to JSONValue.Object(mapOf("type" to JSONValue.String("integer"))))
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("power")))
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val power = TypeResolver.coerceToInt(arguments["power"] ?: JSONValue.Null) ?: 1
        return SampleShowcaseState.asJson(SampleShowcaseState.launchConfetti(power))
    }
}

object RenameMascot : AnyResolveKitFunction {
    override val resolveKitName = "rename_mascot"
    override val resolveKitDescription = "Renames the demo mascot shown in the app"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf("name" to JSONValue.Object(mapOf("type" to JSONValue.String("string"))))
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("name")))
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val name = TypeResolver.coerceToString(arguments["name"] ?: JSONValue.Null) ?: "Robo Otter"
        return SampleShowcaseState.asJson(SampleShowcaseState.renameMascot(name))
    }
}

object ArmLasers : AnyResolveKitFunction {
    override val resolveKitName = "arm_lasers"
    override val resolveKitDescription = "Arms or disarms demo lasers; requires approval"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf("enabled" to JSONValue.Object(mapOf("type" to JSONValue.String("boolean"))))
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("enabled")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = true

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val enabled = TypeResolver.coerceToBool(arguments["enabled"] ?: JSONValue.Null) ?: false
        return SampleShowcaseState.asJson(SampleShowcaseState.armLasers(enabled))
    }
}

object GetShowcaseState : AnyResolveKitFunction {
    override val resolveKitName = "get_showcase_state"
    override val resolveKitDescription = "Returns current demo app state"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(emptyMap())
    )
    override val resolveKitTimeoutSeconds = 5
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        return SampleShowcaseState.asJson(SampleShowcaseState.snapshot())
    }
}

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

object AddNumbers : AnyResolveKitFunction {
    override val resolveKitName = "add_numbers"
    override val resolveKitDescription = "Adds two numbers and returns the result"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf(
                "a" to JSONValue.Object(mapOf("type" to JSONValue.String("number"))),
                "b" to JSONValue.Object(mapOf("type" to JSONValue.String("number")))
            )
        ),
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

object DeleteData : AnyResolveKitFunction {
    override val resolveKitName = "delete_data"
    override val resolveKitDescription = "Deletes specified data. This action requires user approval."
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf("item_id" to JSONValue.Object(mapOf("type" to JSONValue.String("string"))))
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("item_id")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = true

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val itemId = TypeResolver.coerceToString(arguments["item_id"] ?: JSONValue.Null) ?: ""
        return JSONValue.Object(
            mapOf(
                "deleted" to JSONValue.Bool(true),
                "item_id" to JSONValue.String(itemId)
            )
        )
    }
}

object GetWeather : AnyResolveKitFunction {
    override val resolveKitName = "get_weather"
    override val resolveKitDescription = "Returns the current weather for a specified city"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf(
                "city" to JSONValue.Object(
                    mapOf(
                        "type" to JSONValue.String("string"),
                        "description" to JSONValue.String("The city name (e.g., 'London', 'Tokyo')")
                    )
                )
            )
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("city")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val city = TypeResolver.coerceToString(arguments["city"] ?: JSONValue.Null) ?: "Unknown"
        val conditions = listOf("Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Clear")
        val temp = (10..30).random()
        val condition = conditions.random()
        return JSONValue.Object(
            mapOf(
                "city" to JSONValue.String(city),
                "temperature" to JSONValue.Number(temp.toDouble()),
                "unit" to JSONValue.String("°C"),
                "condition" to JSONValue.String(condition),
                "note" to JSONValue.String("Simulated data — integrate a real weather API in production")
            )
        )
    }
}

object SearchNotes : AnyResolveKitFunction {
    override val resolveKitName = "search_notes"
    override val resolveKitDescription = "Searches notes by keyword and returns matching results"
    override val resolveKitParametersSchema: JSONObject = mapOf(
        "type" to JSONValue.String("object"),
        "properties" to JSONValue.Object(
            mapOf(
                "query" to JSONValue.Object(
                    mapOf(
                        "type" to JSONValue.String("string"),
                        "description" to JSONValue.String("The search keyword")
                    )
                ),
                "limit" to JSONValue.Object(
                    mapOf(
                        "type" to JSONValue.String("integer"),
                        "description" to JSONValue.String("Maximum number of results (default: 5)")
                    )
                )
            )
        ),
        "required" to JSONValue.Array(listOf(JSONValue.String("query")))
    )
    override val resolveKitTimeoutSeconds = 10
    override val resolveKitRequiresApproval = false

    override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
        val query = TypeResolver.coerceToString(arguments["query"] ?: JSONValue.Null) ?: ""
        val limit = (TypeResolver.coerceToInt(arguments["limit"] ?: JSONValue.Null) ?: 5).coerceIn(1, 20)

        val sampleNotes = listOf(
            mapOf("title" to "Meeting Notes", "content" to "Discuss project timeline"),
            mapOf("title" to "Shopping List", "content" to "Milk, eggs, bread"),
            mapOf("title" to "Travel Plans", "content" to "Flight to Tokyo on March 15"),
            mapOf("title" to "Code Review", "content" to "Review pull request #42"),
            mapOf("title" to "Birthday Party", "content" to "Order cake for Saturday")
        )

        val results = sampleNotes
            .filter {
                it["title"]?.contains(query, ignoreCase = true) == true ||
                    it["content"]?.contains(query, ignoreCase = true) == true
            }
            .take(limit)

        return JSONValue.Array(
            results.map { note ->
                JSONValue.Object(note.mapValues { JSONValue.String(it.value) })
            }
        )
    }
}

object SampleUtilityFunctionPack : ResolveKitFunctionPack {
    override val packName: String = "sample_demo_tools"
    override val supportedPlatforms: List<ResolveKitPlatform> = listOf(ResolveKitPlatform.ANDROID)
    override val functions: List<AnyResolveKitFunction> = listOf(
        SetDemoVibe,
        LaunchConfetti,
        RenameMascot,
        ArmLasers,
        GetShowcaseState,
        EchoMessageResolveKitAdapter
    )
}
