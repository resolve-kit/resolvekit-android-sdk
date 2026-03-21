package app.resolvekit.ui

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class ResolveKitChatHostRegistryTest {

    @Test
    fun `registry resolves registered factory`() {
        val factory = ResolveKitRuntimeFactory { _ -> error("not used in unit test") }
        val id = ResolveKitChatHostRegistry.register(factory)

        assertSame(factory, ResolveKitChatHostRegistry.resolve(id))

        ResolveKitChatHostRegistry.unregister(id)
    }

    @Test
    fun `registry returns null after unregister`() {
        val factory = ResolveKitRuntimeFactory { _ -> error("not used in unit test") }
        val id = ResolveKitChatHostRegistry.register(factory)

        ResolveKitChatHostRegistry.unregister(id)

        assertNull(ResolveKitChatHostRegistry.resolve(id))
    }
}
