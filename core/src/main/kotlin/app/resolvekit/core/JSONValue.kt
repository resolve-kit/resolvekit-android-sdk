package app.resolvekit.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

typealias JSONObject = Map<String, JSONValue>

@Serializable(with = JSONValueSerializer::class)
sealed class JSONValue {

    data class String(val value: kotlin.String) : JSONValue()
    data class Number(val value: Double) : JSONValue()
    data class Bool(val value: Boolean) : JSONValue()
    data class Object(val value: Map<kotlin.String, JSONValue>) : JSONValue()
    data class Array(val value: List<JSONValue>) : JSONValue()
    object Null : JSONValue()

    val stringValue: kotlin.String? get() = (this as? String)?.value
    val doubleValue: Double? get() = (this as? Number)?.value
    val boolValue: Boolean? get() = (this as? Bool)?.value
    val objectValue: Map<kotlin.String, JSONValue>? get() = (this as? Object)?.value
    val arrayValue: List<JSONValue>? get() = (this as? Array)?.value

    fun toKotlinValue(): Any? = when (this) {
        is String -> value
        is Number -> value
        is Bool -> value
        is Object -> value.mapValues { it.value.toKotlinValue() }
        is Array -> value.map { it.toKotlinValue() }
        is Null -> null
    }

    companion object {
        fun fromJsonElement(element: JsonElement): JSONValue = when (element) {
            is JsonNull -> Null
            is JsonPrimitive -> when {
                element.isString -> String(element.content)
                element.booleanOrNull != null -> Bool(element.boolean)
                else -> Number(element.double)
            }
            is JsonObject -> Object(element.mapValues { fromJsonElement(it.value) })
            is JsonArray -> Array(element.map { fromJsonElement(it) })
        }

        fun toJsonElement(value: JSONValue): JsonElement = when (value) {
            is String -> JsonPrimitive(value.value)
            is Number -> JsonPrimitive(value.value)
            is Bool -> JsonPrimitive(value.value)
            is Object -> JsonObject(value.value.mapValues { toJsonElement(it.value) })
            is Array -> JsonArray(value.value.map { toJsonElement(it) })
            is Null -> JsonNull
        }
    }
}

object JSONValueSerializer : KSerializer<JSONValue> {
    private val delegate = JsonElement.serializer()
    override val descriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: JSONValue) =
        encoder.encodeSerializableValue(delegate, JSONValue.toJsonElement(value))

    override fun deserialize(decoder: Decoder): JSONValue =
        JSONValue.fromJsonElement(decoder.decodeSerializableValue(delegate))
}
