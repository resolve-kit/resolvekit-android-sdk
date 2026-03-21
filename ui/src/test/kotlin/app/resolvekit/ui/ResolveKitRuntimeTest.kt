package app.resolvekit.ui

import app.resolvekit.ui.models.ResolveKitConnectionState
import app.resolvekit.ui.models.ResolveKitToolCallBatchState
import org.junit.Assert.*
import org.junit.Test

class ResolveKitRuntimeTest {

    private fun testConfig() = ResolveKitConfiguration(apiKeyProvider = { "test-key" })

    @Test
    fun `initial connectionState is IDLE`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertEquals(ResolveKitConnectionState.IDLE, runtime.connectionState.value)
    }

    @Test
    fun `initial messages is empty`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertTrue(runtime.messages.value.isEmpty())
    }

    @Test
    fun `initial isTurnInProgress is false`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertFalse(runtime.isTurnInProgress.value)
    }

    @Test
    fun `initial toolCallBatchState is IDLE`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertEquals(ResolveKitToolCallBatchState.IDLE, runtime.toolCallBatchState.value)
    }

    @Test
    fun `initial toolCallChecklist is empty`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertTrue(runtime.toolCallChecklist.value.isEmpty())
    }

    @Test
    fun `initial toolCallBatches is empty`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertTrue(runtime.toolCallBatches.value.isEmpty())
    }

    @Test
    fun `initial lastError is null`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertNull(runtime.lastError.value)
    }

    @Test
    fun `initial chatTheme uses sdk fallback`() {
        val runtime = ResolveKitRuntime(testConfig())
        assertEquals(ResolveKitTheme.fallbackTheme, runtime.chatTheme.value)
    }

    @Test
    fun `setAppearance updates appearanceMode`() {
        val runtime = ResolveKitRuntime(testConfig())
        runtime.setAppearance(app.resolvekit.ui.models.ResolveKitAppearanceMode.DARK)
        assertEquals(app.resolvekit.ui.models.ResolveKitAppearanceMode.DARK, runtime.appearanceMode.value)
    }

    @Test
    fun `stop transitions to IDLE`() {
        val runtime = ResolveKitRuntime(testConfig())
        runtime.stop()
        assertEquals(ResolveKitConnectionState.IDLE, runtime.connectionState.value)
    }

    @Test
    fun `configuration allFunctions combines functions and packs`() {
        val fn1 = object : app.resolvekit.core.AnyResolveKitFunction {
            override val resolveKitName = "fn1"
            override val resolveKitDescription = ""
            override val resolveKitParametersSchema = emptyMap<String, app.resolvekit.core.JSONValue>()
            override val resolveKitTimeoutSeconds: Int? = null
            override val resolveKitRequiresApproval = false
            override suspend fun invoke(arguments: app.resolvekit.core.JSONObject, context: app.resolvekit.core.ResolveKitFunctionContext) = app.resolvekit.core.JSONValue.Null
        }
        val fn2 = object : app.resolvekit.core.AnyResolveKitFunction {
            override val resolveKitName = "fn2"
            override val resolveKitDescription = ""
            override val resolveKitParametersSchema = emptyMap<String, app.resolvekit.core.JSONValue>()
            override val resolveKitTimeoutSeconds: Int? = null
            override val resolveKitRequiresApproval = false
            override suspend fun invoke(arguments: app.resolvekit.core.JSONObject, context: app.resolvekit.core.ResolveKitFunctionContext) = app.resolvekit.core.JSONValue.Null
        }
        val pack = object : app.resolvekit.core.ResolveKitFunctionPack {
            override val packName = "pack1"
            override val supportedPlatforms = listOf(app.resolvekit.core.ResolveKitPlatform.ANDROID)
            override val functions = listOf(fn2)
        }
        val config = ResolveKitConfiguration(
            apiKeyProvider = { "key" },
            functions = listOf(fn1),
            functionPacks = listOf(pack)
        )
        assertEquals(2, config.allFunctions.size)
        assertTrue(config.allFunctions.any { it.resolveKitName == "fn1" })
        assertTrue(config.allFunctions.any { it.resolveKitName == "fn2" })
    }
}
