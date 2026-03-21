package app.resolvekit.core

import org.junit.Assert.*
import org.junit.Test

class JSONValueTest {

    @Test
    fun `string value roundtrips`() {
        val v = JSONValue.String("hello")
        assertEquals("hello", v.stringValue)
    }

    @Test
    fun `number value roundtrips`() {
        val v = JSONValue.Number(42.0)
        assertEquals(42.0, v.doubleValue!!, 0.001)
    }

    @Test
    fun `bool value roundtrips`() {
        val v = JSONValue.Bool(true)
        assertEquals(true, v.boolValue)
    }

    @Test
    fun `object value access`() {
        val obj = JSONValue.Object(mapOf("key" to JSONValue.String("val")))
        assertEquals(JSONValue.String("val"), obj.objectValue?.get("key"))
    }

    @Test
    fun `array value access`() {
        val arr = JSONValue.Array(listOf(JSONValue.Number(1.0), JSONValue.Number(2.0)))
        assertEquals(2, arr.arrayValue?.size)
    }

    @Test
    fun `null value`() {
        assertNull(JSONValue.Null.stringValue)
        assertNull(JSONValue.Null.doubleValue)
    }

    @Test
    fun `toKotlinValue string`() {
        assertEquals("hello", JSONValue.String("hello").toKotlinValue())
    }

    @Test
    fun `toKotlinValue number`() {
        assertEquals(42.0, JSONValue.Number(42.0).toKotlinValue())
    }

    @Test
    fun `toKotlinValue null`() {
        assertNull(JSONValue.Null.toKotlinValue())
    }

    @Test
    fun `toKotlinValue nested object`() {
        val obj = JSONValue.Object(mapOf("x" to JSONValue.Number(1.0)))
        val result = obj.toKotlinValue() as Map<*, *>
        assertEquals(1.0, result["x"])
    }
}
