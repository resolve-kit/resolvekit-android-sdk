package app.resolvekit.ksp

import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Generates a Kotlin source string representing a JSONObject (Map<String, JSONValue>)
 * that encodes the JSON Schema for a set of constructor parameters.
 */
class SchemaGenerator {

    fun generateSchema(params: List<KSValueParameter>): String {
        if (params.isEmpty()) {
            return """mapOf(
    "type" to JSONValue.String("object"),
    "properties" to JSONValue.Object(emptyMap())
)"""
        }

        val required = params
            .filter { !it.type.resolve().isMarkedNullable }
            .joinToString(", ") { "JSONValue.String(\"${it.name?.asString()}\")" }

        val properties = params.joinToString(",\n") { param ->
            val paramName = param.name?.asString() ?: return@joinToString ""
            val typeName = param.type.resolve().declaration.simpleName.asString()
            val jsonType = typeToJsonSchema(typeName)
            "        \"$paramName\" to JSONValue.Object(mapOf(\"type\" to JSONValue.String(\"$jsonType\")))"
        }

        val requiredLine = if (required.isNotEmpty()) {
            """,
    "required" to JSONValue.Array(listOf($required))"""
        } else ""

        return """mapOf(
    "type" to JSONValue.String("object"),
    "properties" to JSONValue.Object(mapOf(
$properties
    ))$requiredLine
)"""
    }

    private fun typeToJsonSchema(kotlinType: String): String = when (kotlinType) {
        "String" -> "string"
        "Boolean" -> "boolean"
        "Int", "Long", "Short", "Byte" -> "integer"
        "Double", "Float" -> "number"
        else -> "string"
    }

    fun coercionCall(typeName: String, paramName: String): String = when (typeName) {
        "String" -> "TypeResolver.coerceToString(arguments[\"$paramName\"] ?: JSONValue.Null) ?: \"\""
        "Boolean" -> "TypeResolver.coerceToBool(arguments[\"$paramName\"] ?: JSONValue.Null) ?: false"
        "Int" -> "TypeResolver.coerceToInt(arguments[\"$paramName\"] ?: JSONValue.Null) ?: 0"
        "Long" -> "TypeResolver.coerceToLong(arguments[\"$paramName\"] ?: JSONValue.Null) ?: 0L"
        "Double" -> "TypeResolver.coerceToDouble(arguments[\"$paramName\"] ?: JSONValue.Null) ?: 0.0"
        "Float" -> "TypeResolver.coerceToFloat(arguments[\"$paramName\"] ?: JSONValue.Null) ?: 0f"
        else -> "TypeResolver.coerceToString(arguments[\"$paramName\"] ?: JSONValue.Null) ?: \"\""
    }

    fun coercionCallNullable(typeName: String, paramName: String): String = when (typeName) {
        "String" -> "TypeResolver.coerceToString(arguments[\"$paramName\"] ?: JSONValue.Null)"
        "Boolean" -> "TypeResolver.coerceToBool(arguments[\"$paramName\"] ?: JSONValue.Null)"
        "Int" -> "TypeResolver.coerceToInt(arguments[\"$paramName\"] ?: JSONValue.Null)"
        "Long" -> "TypeResolver.coerceToLong(arguments[\"$paramName\"] ?: JSONValue.Null)"
        "Double" -> "TypeResolver.coerceToDouble(arguments[\"$paramName\"] ?: JSONValue.Null)"
        "Float" -> "TypeResolver.coerceToFloat(arguments[\"$paramName\"] ?: JSONValue.Null)"
        else -> "TypeResolver.coerceToString(arguments[\"$paramName\"] ?: JSONValue.Null)"
    }
}
