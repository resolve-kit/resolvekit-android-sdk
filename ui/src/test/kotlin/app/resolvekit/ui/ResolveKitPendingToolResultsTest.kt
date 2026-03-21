package app.resolvekit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveKitPendingToolResultsTest {

    @Test
    fun `enqueue replaces payload with same call id`() {
        val queue = ResolveKitPendingToolResults()

        queue.enqueue(PendingToolResult(callId = "call-1", turnId = "turn-1", status = "success"))
        queue.enqueue(PendingToolResult(callId = "call-1", turnId = "turn-1", status = "error"))

        val snapshot = queue.snapshot()
        assertEquals(1, snapshot.size)
        assertEquals("error", snapshot.single().status)
    }

    @Test
    fun `removeAll removes submitted payloads`() {
        val queue = ResolveKitPendingToolResults()

        queue.enqueue(PendingToolResult(callId = "call-1", turnId = "turn-1", status = "success"))
        queue.enqueue(PendingToolResult(callId = "call-2", turnId = "turn-1", status = "error"))
        queue.removeAll(setOf("call-1"))

        val snapshot = queue.snapshot()
        assertEquals(1, snapshot.size)
        assertEquals("call-2", snapshot.single().callId)
    }

    @Test
    fun `snapshot is empty by default`() {
        val queue = ResolveKitPendingToolResults()
        assertTrue(queue.snapshot().isEmpty())
    }
}
