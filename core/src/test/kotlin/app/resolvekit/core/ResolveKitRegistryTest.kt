package app.resolvekit.core

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResolveKitRegistryTest {

    private lateinit var registry: ResolveKitRegistry

    private val echoFn = object : AnyResolveKitFunction {
        override val resolveKitName = "echo"
        override val resolveKitDescription = "Echo input"
        override val resolveKitParametersSchema: JSONObject = mapOf(
            "type" to JSONValue.String("object"),
            "properties" to JSONValue.Object(emptyMap())
        )
        override val resolveKitTimeoutSeconds: Int? = null
        override val resolveKitRequiresApproval = false
        override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext) =
            arguments["text"] ?: JSONValue.Null
    }

    private val pingFn = object : AnyResolveKitFunction {
        override val resolveKitName = "ping"
        override val resolveKitDescription = "Ping"
        override val resolveKitParametersSchema: JSONObject = emptyMap()
        override val resolveKitTimeoutSeconds: Int? = 5
        override val resolveKitRequiresApproval = true
        override suspend fun invoke(arguments: JSONObject, context: ResolveKitFunctionContext) =
            JSONValue.String("pong")
    }

    @Before
    fun setup() {
        registry = ResolveKitRegistry()
    }

    @Test
    fun `register and resolve function`() = runTest {
        registry.register(echoFn)
        assertNotNull(registry.resolve("echo"))
    }

    @Test
    fun `size tracks registered count`() = runTest {
        assertEquals(0, registry.size)
        registry.register(echoFn)
        assertEquals(1, registry.size)
        registry.register(pingFn)
        assertEquals(2, registry.size)
    }

    @Test
    fun `duplicate name throws`() = runTest {
        registry.register(echoFn)
        try {
            registry.register(echoFn)
            fail("Expected DuplicateFunctionName")
        } catch (e: ResolveKitFunctionError.DuplicateFunctionName) {
            assertEquals("echo", e.name)
        }
    }

    @Test
    fun `dispatch returns correct result`() = runTest {
        registry.register(echoFn)
        val ctx = ResolveKitFunctionContext("s1", "r1")
        val result = registry.dispatch("echo", mapOf("text" to JSONValue.String("hi")), ctx)
        assertEquals(JSONValue.String("hi"), result)
    }

    @Test
    fun `dispatch unknown function throws`() = runTest {
        try {
            registry.dispatch("nope", emptyMap(), ResolveKitFunctionContext("s", null))
            fail("Expected UnknownFunction")
        } catch (e: ResolveKitFunctionError.UnknownFunction) {
            assertEquals("nope", e.name)
        }
    }

    @Test
    fun `resolve returns null for missing function`() {
        assertNull(registry.resolve("missing"))
    }

    @Test
    fun `reset clears all functions`() = runTest {
        registry.register(echoFn)
        registry.register(pingFn)
        registry.reset()
        assertEquals(0, registry.size)
        assertNull(registry.resolve("echo"))
    }

    @Test
    fun `definitions returns metadata for all functions`() = runTest {
        registry.register(echoFn)
        registry.register(pingFn)
        val defs = registry.definitions
        assertEquals(2, defs.size)
        assertTrue(defs.any { it.name == "echo" })
        assertTrue(defs.any { it.name == "ping" })
    }
}
