package app.resolvekit.core

import org.junit.Assert.*
import org.junit.Test

class TypeResolverTest {

    @Test
    fun `coerce string number to int`() {
        assertEquals(42, TypeResolver.coerceToInt(JSONValue.String("42")))
    }

    @Test
    fun `coerce number to int`() {
        assertEquals(7, TypeResolver.coerceToInt(JSONValue.Number(7.9)))
    }

    @Test
    fun `coerce bool true to int`() {
        assertEquals(1, TypeResolver.coerceToInt(JSONValue.Bool(true)))
    }

    @Test
    fun `coerce string true to bool`() {
        assertEquals(true, TypeResolver.coerceToBool(JSONValue.String("true")))
    }

    @Test
    fun `coerce string yes to bool`() {
        assertEquals(true, TypeResolver.coerceToBool(JSONValue.String("yes")))
    }

    @Test
    fun `coerce number 1 to bool`() {
        assertEquals(true, TypeResolver.coerceToBool(JSONValue.Number(1.0)))
    }

    @Test
    fun `coerce number 0 to bool false`() {
        assertEquals(false, TypeResolver.coerceToBool(JSONValue.Number(0.0)))
    }

    @Test
    fun `coerce number to string integer`() {
        assertEquals("42", TypeResolver.coerceToString(JSONValue.Number(42.0)))
    }

    @Test
    fun `coerce number to string float`() {
        assertEquals("3.14", TypeResolver.coerceToString(JSONValue.Number(3.14)))
    }

    @Test
    fun `coerce bool to string`() {
        assertEquals("true", TypeResolver.coerceToString(JSONValue.Bool(true)))
    }

    @Test
    fun `null stays null for string`() {
        assertNull(TypeResolver.coerceToString(JSONValue.Null))
    }

    @Test
    fun `null stays null for bool`() {
        assertNull(TypeResolver.coerceToBool(JSONValue.Null))
    }

    @Test
    fun `coerce to double`() {
        assertEquals(3.14, TypeResolver.coerceToDouble(JSONValue.Number(3.14))!!, 0.001)
    }

    @Test
    fun `coerce string to double`() {
        assertEquals(2.5, TypeResolver.coerceToDouble(JSONValue.String("2.5"))!!, 0.001)
    }

    @Test
    fun `coerce to long`() {
        assertEquals(100L, TypeResolver.coerceToLong(JSONValue.Number(100.0)))
    }
}
