package app.resolvekit.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import app.resolvekit.networking.models.ChatTheme
import app.resolvekit.networking.models.ChatThemePalette
import app.resolvekit.ui.models.ResolveKitAppearanceMode

object ResolveKitTheme {
    val fallbackTheme = ChatTheme(
        light = ChatThemePalette(
            screenBackground = "#F7F7FA",
            titleText = "#111827",
            statusText = "#4B5563",
            composerBackground = "#FFFFFF",
            composerText = "#111827",
            composerPlaceholder = "#9CA3AF",
            userBubbleBackground = "#DBEAFE",
            userBubbleText = "#1E3A8A",
            assistantBubbleBackground = "#E5E7EB",
            assistantBubbleText = "#111827",
            loaderBubbleBackground = "#E5E7EB",
            loaderDotActive = "#374151",
            loaderDotInactive = "#9CA3AF",
            toolCardBackground = "#FFFFFFCC",
            toolCardBorder = "#D1D5DB",
            toolCardTitle = "#111827",
            toolCardBody = "#374151"
        ),
        dark = ChatThemePalette(
            screenBackground = "#0B0C10",
            titleText = "#E5E7EB",
            statusText = "#9CA3AF",
            composerBackground = "#111318",
            composerText = "#E5E7EB",
            composerPlaceholder = "#6B7280",
            userBubbleBackground = "#1E3A8A99",
            userBubbleText = "#DBEAFE",
            assistantBubbleBackground = "#1F2937",
            assistantBubbleText = "#E5E7EB",
            loaderBubbleBackground = "#1F2937",
            loaderDotActive = "#E5E7EB",
            loaderDotInactive = "#6B7280",
            toolCardBackground = "#111318CC",
            toolCardBorder = "#374151",
            toolCardTitle = "#E5E7EB",
            toolCardBody = "#9CA3AF"
        )
    )

    fun resolvePalette(
        theme: ChatTheme?,
        appearanceMode: ResolveKitAppearanceMode,
        systemIsDark: Boolean
    ): ChatThemePalette {
        val activeTheme = theme ?: fallbackTheme
        return when (appearanceMode) {
            ResolveKitAppearanceMode.LIGHT -> activeTheme.light
            ResolveKitAppearanceMode.DARK -> activeTheme.dark
            ResolveKitAppearanceMode.SYSTEM -> if (systemIsDark) activeTheme.dark else activeTheme.light
        }
    }

    fun resolvePaletteColors(
        theme: ChatTheme?,
        appearanceMode: ResolveKitAppearanceMode,
        systemIsDark: Boolean
    ): ResolveKitPaletteColors {
        return resolvePalette(theme, appearanceMode, systemIsDark).toPaletteColors()
    }

    fun parseHexColor(hex: String, fallback: Color = Color.Unspecified): Color {
        val raw = hex.trim().removePrefix("#")
        val value = raw.toULongOrNull(16) ?: return fallback
        return when (raw.length) {
            6 -> Color(
                red = ((value shr 16) and 0xFFu).toInt(),
                green = ((value shr 8) and 0xFFu).toInt(),
                blue = (value and 0xFFu).toInt(),
                alpha = 0xFF
            )
            8 -> Color(
                red = ((value shr 24) and 0xFFu).toInt(),
                green = ((value shr 16) and 0xFFu).toInt(),
                blue = ((value shr 8) and 0xFFu).toInt(),
                alpha = (value and 0xFFu).toInt()
            )
            else -> fallback
        }
    }
}

data class ResolveKitPaletteColors(
    val screenBackground: Color,
    val titleText: Color,
    val statusText: Color,
    val composerBackground: Color,
    val composerText: Color,
    val composerPlaceholder: Color,
    val userBubbleBackground: Color,
    val userBubbleText: Color,
    val assistantBubbleBackground: Color,
    val assistantBubbleText: Color,
    val loaderBubbleBackground: Color,
    val loaderDot1: Color,
    val loaderDot2: Color,
    val loaderDot3: Color,
    val toolCardBackground: Color,
    val toolCardBorder: Color,
    val toolCardText: Color,
    val toolCardStatus: Color
)

fun ColorScheme.toResolveKitPaletteColors(): ResolveKitPaletteColors = ResolveKitPaletteColors(
    screenBackground = background,
    titleText = onSurface,
    statusText = onSurfaceVariant,
    composerBackground = surface,
    composerText = onSurface,
    composerPlaceholder = onSurfaceVariant,
    userBubbleBackground = primaryContainer,
    userBubbleText = onPrimaryContainer,
    assistantBubbleBackground = secondaryContainer,
    assistantBubbleText = onSecondaryContainer,
    loaderBubbleBackground = secondaryContainer,
    loaderDot1 = onSecondaryContainer.copy(alpha = 0.95f),
    loaderDot2 = onSecondaryContainer.copy(alpha = 0.7f),
    loaderDot3 = onSecondaryContainer.copy(alpha = 0.45f),
    toolCardBackground = surface,
    toolCardBorder = outlineVariant,
    toolCardText = onSurface,
    toolCardStatus = onSurfaceVariant
)

fun ChatThemePalette.toPaletteColors(): ResolveKitPaletteColors = ResolveKitPaletteColors(
    screenBackground = ResolveKitTheme.parseHexColor(screenBackground, Color(0xFFF7F7FA)),
    titleText = ResolveKitTheme.parseHexColor(titleText, Color(0xFF111827)),
    statusText = ResolveKitTheme.parseHexColor(statusText, Color(0xFF4B5563)),
    composerBackground = ResolveKitTheme.parseHexColor(composerBackground, Color.White),
    composerText = ResolveKitTheme.parseHexColor(composerText, Color(0xFF111827)),
    composerPlaceholder = ResolveKitTheme.parseHexColor(composerPlaceholder, Color(0xFF9CA3AF)),
    userBubbleBackground = ResolveKitTheme.parseHexColor(userBubbleBackground, Color(0xFFDBEAFE)),
    userBubbleText = ResolveKitTheme.parseHexColor(userBubbleText, Color(0xFF1E3A8A)),
    assistantBubbleBackground = ResolveKitTheme.parseHexColor(assistantBubbleBackground, Color(0xFFE5E7EB)),
    assistantBubbleText = ResolveKitTheme.parseHexColor(assistantBubbleText, Color(0xFF111827)),
    loaderBubbleBackground = ResolveKitTheme.parseHexColor(loaderBubbleBackground, Color(0xFFE5E7EB)),
    loaderDot1 = ResolveKitTheme.parseHexColor(loaderDotActive, Color(0xFF374151)),
    loaderDot2 = ResolveKitTheme.parseHexColor(loaderDotInactive, Color(0xFF9CA3AF)),
    loaderDot3 = ResolveKitTheme.parseHexColor(loaderDotInactive, Color(0xFF9CA3AF)),
    toolCardBackground = ResolveKitTheme.parseHexColor(toolCardBackground, Color.White),
    toolCardBorder = ResolveKitTheme.parseHexColor(toolCardBorder, Color(0xFFD1D5DB)),
    toolCardText = ResolveKitTheme.parseHexColor(toolCardTitle, Color(0xFF111827)),
    toolCardStatus = ResolveKitTheme.parseHexColor(toolCardBody, Color(0xFF4B5563))
)
