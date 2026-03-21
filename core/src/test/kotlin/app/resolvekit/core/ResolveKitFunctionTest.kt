package app.resolvekit.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ResolveKitFunctionTest {

    /** Minimal echo function for testing. */
    private object EchoFunction : AnyResolveKitFunction {
        override val resolveKitName = "echo"
        override val resolveKitDescription = "Echo back the input text"
        override val resolveKitParametersSchema: JSONObject = mapOf(
            "type" to JSONValue.String("object"),
            "properties" to JSONValue.Object(
                mapOf("text" to JSONValue.Object(mapOf("type" to JSONValue.String("string"))))
            ),
            "required" to JSONValue.Array(listOf(JSONValue.String("text")))
        )
        override val resolveKitTimeoutSeconds: Int? = null
        override val resolveKitRequiresApproval = false

        override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext): JSONValue {
            val text = TypeResolver.coerceToString(arguments["text"] ?: JSONValue.Null) ?: ""
            return JSONValue.String(text)
        }
    }

    @Test
    fun `function has correct name`() {
        assertEquals("echo", EchoFunction.resolveKitName)
    }

    @Test
    fun `function does not require approval`() {
        assertFalse(EchoFunction.resolveKitRequiresApproval)
    }

    @Test
    fun `function invokes and returns string`() = runTest {
        val ctx = ResolveKitFunctionContext("session-1", "req-1")
        val result = EchoFunction.invoke(mapOf("text" to JSONValue.String("hello")), ctx)
        assertEquals(JSONValue.String("hello"), result)
    }

    @Test
    fun `function context carries session id`() {
        val ctx = ResolveKitFunctionContext("sess-abc", "req-xyz")
        assertEquals("sess-abc", ctx.sessionID)
        assertEquals("req-xyz", ctx.requestID)
    }
}
