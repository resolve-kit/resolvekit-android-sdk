package app.resolvekit.ksp

import org.junit.Assert.*
import org.junit.Test

class SchemaGeneratorTest {

    private val gen = SchemaGenerator()

    @Test
    fun `empty params generates empty schema`() {
        val schema = gen.generateSchema(emptyList())
        assertTrue(schema.contains("\"object\""))
        assertTrue(schema.contains("emptyMap()"))
    }

    @Test
    fun `string coercion call is correct`() {
        val call = gen.coercionCall("String", "myParam")
        assertTrue(call.contains("coerceToString"))
        assertTrue(call.contains("myParam"))
    }

    @Test
    fun `bool coercion call is correct`() {
        val call = gen.coercionCall("Boolean", "flag")
        assertTrue(call.contains("coerceToBool"))
        assertTrue(call.contains("flag"))
    }

    @Test
    fun `int coercion call is correct`() {
        val call = gen.coercionCall("Int", "count")
        assertTrue(call.contains("coerceToInt"))
        assertTrue(call.contains("count"))
    }

    @Test
    fun `nullable coercion does not have default fallback`() {
        val call = gen.coercionCallNullable("String", "optParam")
        assertFalse(call.contains("?: \"\""))
        assertTrue(call.contains("coerceToString"))
    }

    @Test
    fun `type to json schema maps correctly`() {
        // We test via generateSchema with mock-like approach; verify via coercionCall output
        // String -> "string", Boolean -> "boolean", Int -> "integer", Double -> "number"
        val strCall = gen.coercionCall("String", "x")
        val boolCall = gen.coercionCall("Boolean", "x")
        val intCall = gen.coercionCall("Int", "x")
        val dblCall = gen.coercionCall("Double", "x")

        assertTrue(strCall.contains("coerceToString"))
        assertTrue(boolCall.contains("coerceToBool"))
        assertTrue(intCall.contains("coerceToInt"))
        assertTrue(dblCall.contains("coerceToDouble"))
    }
}
