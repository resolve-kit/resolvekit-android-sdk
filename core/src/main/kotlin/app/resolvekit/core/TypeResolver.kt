package app.resolvekit.core

import kotlin.math.floor

object TypeResolver {

    fun coerceToString(value: JSONValue): String? = when (value) {
        is JSONValue.String -> value.value
        is JSONValue.Number -> {
            val d = value.value
            if (!d.isInfinite() && !d.isNaN() && d == floor(d)) d.toLong().toString() else d.toString()
        }
        is JSONValue.Bool -> value.value.toString()
        is JSONValue.Null -> null
        else -> null
    }

    fun coerceToBool(value: JSONValue): Boolean? = when (value) {
        is JSONValue.Bool -> value.value
        is JSONValue.Number -> value.value != 0.0
        is JSONValue.String -> when (value.value.lowercase().trim()) {
            "true", "yes", "1" -> true
            "false", "no", "0" -> false
            else -> null
        }
        is JSONValue.Null -> null
        else -> null
    }

    fun coerceToInt(value: JSONValue): Int? = when (value) {
        is JSONValue.Number -> value.value.toInt()
        is JSONValue.String -> value.value.toDoubleOrNull()?.toInt()
        is JSONValue.Bool -> if (value.value) 1 else 0
        else -> null
    }

    fun coerceToLong(value: JSONValue): Long? = when (value) {
        is JSONValue.Number -> value.value.toLong()
        is JSONValue.String -> value.value.toDoubleOrNull()?.toLong()
        is JSONValue.Bool -> if (value.value) 1L else 0L
        else -> null
    }

    fun coerceToDouble(value: JSONValue): Double? = when (value) {
        is JSONValue.Number -> value.value
        is JSONValue.String -> value.value.toDoubleOrNull()
        is JSONValue.Bool -> if (value.value) 1.0 else 0.0
        else -> null
    }

    fun coerceToFloat(value: JSONValue): Float? = coerceToDouble(value)?.toFloat()
}
