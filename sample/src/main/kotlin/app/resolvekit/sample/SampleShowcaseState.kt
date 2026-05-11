package app.resolvekit.sample

import app.resolvekit.core.JSONValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class DemoAppState(
    val vibe: String = "Chill",
    val accent: String = "Cyan",
    val mascot: String = "Robo Otter",
    val confettiBursts: Int = 0,
    val lasersArmed: Boolean = false
)

object SampleShowcaseState {
    private val _state = MutableStateFlow(DemoAppState())
    val state: StateFlow<DemoAppState> = _state.asStateFlow()

    fun snapshot(): DemoAppState = _state.value

    fun setVibe(input: String): DemoAppState {
        val normalized = input.trim().lowercase(Locale.ROOT)
        _state.value = when (normalized) {
            "neon", "cyber", "party" -> _state.value.copy(vibe = "Neon Surge", accent = "Electric Pink")
            "chaos", "wild", "rad" -> _state.value.copy(vibe = "Chaos Mode", accent = "Lime")
            else -> _state.value.copy(vibe = "Chill", accent = "Cyan")
        }
        return _state.value
    }

    fun launchConfetti(power: Int): DemoAppState {
        val bursts = power.coerceIn(1, 20)
        _state.value = _state.value.copy(confettiBursts = _state.value.confettiBursts + bursts)
        return _state.value
    }

    fun renameMascot(name: String): DemoAppState {
        val safeName = name.trim().ifEmpty { "Robo Otter" }
        _state.value = _state.value.copy(mascot = safeName)
        return _state.value
    }

    fun armLasers(enabled: Boolean): DemoAppState {
        _state.value = _state.value.copy(lasersArmed = enabled)
        return _state.value
    }

    fun asJson(state: DemoAppState): JSONValue.Object {
        return JSONValue.Object(
            mapOf(
                "vibe" to JSONValue.String(state.vibe),
                "accent" to JSONValue.String(state.accent),
                "mascot" to JSONValue.String(state.mascot),
                "confetti_bursts" to JSONValue.Number(state.confettiBursts.toDouble()),
                "lasers_armed" to JSONValue.Bool(state.lasersArmed)
            )
        )
    }
}
