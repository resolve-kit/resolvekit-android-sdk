package app.resolvekit.networking

import app.resolvekit.networking.models.ResolveKitEvent
import org.junit.Assert.*
import org.junit.Test

class ResolveKitEventStreamClientTest {

    private fun parse(json: String): ResolveKitEvent? =
        ResolveKitEventStreamClient.parseEventData(json)

    @Test
    fun `parses text delta event`() {
        val data = """{"event_id":"1","type":"assistant_text_delta","payload":{"delta":"Hi","accumulated":"Hi"}}"""
        val event = parse(data)
        assertNotNull(event)
        assertTrue(event is ResolveKitEvent.TextDelta)
        val delta = event as ResolveKitEvent.TextDelta
        assertEquals("1", delta.eventId)
        assertEquals("Hi", delta.delta)
        assertEquals("Hi", delta.accumulated)
    }

    @Test
    fun `parses tool call request event`() {
        val data = """
            {
              "event_id":"2","turn_id":"t1","type":"tool_call_request",
              "payload":{
                "call_id":"c1","function_name":"echo","arguments":{},"timeout_seconds":30,
                "human_description":"Echo something","requires_approval":true
              }
            }
        """.trimIndent()
        val event = parse(data)
        assertTrue(event is ResolveKitEvent.ToolCallRequest)
        val tcr = event as ResolveKitEvent.ToolCallRequest
        assertEquals("c1", tcr.payload.callId)
        assertEquals("echo", tcr.payload.functionName)
        assertTrue(tcr.payload.requiresApproval)
    }

    @Test
    fun `parses turn complete event`() {
        val data = """{"event_id":"3","turn_id":"t1","type":"turn_complete","payload":{"full_text":"Done!"}}"""
        val event = parse(data)
        assertTrue(event is ResolveKitEvent.TurnComplete)
        assertEquals("Done!", (event as ResolveKitEvent.TurnComplete).fullText)
    }

    @Test
    fun `parses error event`() {
        val data = """{"event_id":"4","type":"error","payload":{"code":"transport_error","message":"Oops","recoverable":false}}"""
        val event = parse(data)
        assertTrue(event is ResolveKitEvent.ServerError)
        val err = event as ResolveKitEvent.ServerError
        assertEquals("transport_error", err.code)
        assertFalse(err.recoverable)
    }

    @Test
    fun `returns unknown for unrecognized event type`() {
        val data = """{"event_id":"5","type":"future_event","payload":{}}"""
        val event = parse(data)
        assertTrue(event is ResolveKitEvent.Unknown)
        assertEquals("future_event", (event as ResolveKitEvent.Unknown).type)
    }

    @Test
    fun `returns null for malformed json`() {
        val event = parse("not json at all")
        assertNull(event)
    }

    @Test
    fun `turn_id is nullable`() {
        val data = """{"event_id":"6","type":"assistant_text_delta","payload":{"delta":"x","accumulated":"x"}}"""
        val event = parse(data) as ResolveKitEvent.TextDelta
        assertNull(event.turnId)
    }
}
