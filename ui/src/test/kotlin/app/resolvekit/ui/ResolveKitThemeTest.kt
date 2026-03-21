package app.resolvekit.ui

import androidx.compose.ui.graphics.Color
import app.resolvekit.networking.models.ChatTheme
import app.resolvekit.networking.models.ChatThemePalette
import app.resolvekit.ui.models.ResolveKitAppearanceMode
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveKitThemeTest {

    @Test
    fun `resolvePaletteColors uses sdk fallback colors when theme is null and appearance is SYSTEM`() {
        val resolved = ResolveKitTheme.resolvePaletteColors(
            theme = null,
            appearanceMode = ResolveKitAppearanceMode.SYSTEM,
            systemIsDark = false
        )

        assertEquals(0xFFF7F7FA.toInt(), resolved.screenBackground.toArgb())
        assertEquals(0xFFDBEAFE.toInt(), resolved.userBubbleBackground.toArgb())
        assertEquals(0xFF111827.toInt(), resolved.assistantBubbleText.toArgb())
    }

    @Test
    fun `resolve kit palette colors exposes tool card border token`() {
        val fieldNames = ResolveKitPaletteColors::class.java.declaredFields.map { it.name }

        assertTrue(fieldNames.contains("toolCardBorder"))
    }

    @Test
    fun `resolvePalette uses light theme when appearance is LIGHT`() {
        val resolved = ResolveKitTheme.resolvePalette(
            theme = testTheme(),
            appearanceMode = ResolveKitAppearanceMode.LIGHT,
            systemIsDark = true
        )

        assertEquals("#FFFFFF", resolved.screenBackground)
        assertEquals("#111111", resolved.titleText)
    }

    @Test
    fun `resolvePalette uses dark theme when appearance is SYSTEM and system is dark`() {
        val resolved = ResolveKitTheme.resolvePalette(
            theme = testTheme(),
            appearanceMode = ResolveKitAppearanceMode.SYSTEM,
            systemIsDark = true
        )

        assertEquals("#000000", resolved.screenBackground)
        assertEquals("#EEEEEE", resolved.titleText)
    }

    @Test
    fun `resolvePalette falls back when theme is null`() {
        val resolved = ResolveKitTheme.resolvePalette(
            theme = null,
            appearanceMode = ResolveKitAppearanceMode.SYSTEM,
            systemIsDark = false
        )

        assertFalse(resolved.screenBackground.isBlank())
        assertFalse(resolved.userBubbleBackground.isBlank())
    }

    @Test
    fun `hex parser supports rgb and rgba`() {
        val rgb = ResolveKitTheme.parseHexColor("#112233")
        val rgba = ResolveKitTheme.parseHexColor("#11223344")

        assertEquals(0xFF112233.toInt(), rgb.toArgb())
        assertEquals(0x44112233, rgba.toArgb())
    }

    private fun testTheme() = ChatTheme(
        light = ChatThemePalette(
            screenBackground = "#FFFFFF",
            titleText = "#111111",
            statusText = "#222222",
            composerBackground = "#F3F3F3",
            composerText = "#111111",
            composerPlaceholder = "#777777",
            userBubbleBackground = "#CCE0FF",
            userBubbleText = "#001133",
            assistantBubbleBackground = "#EFEFEF",
            assistantBubbleText = "#222222",
            loaderBubbleBackground = "#DDDDDD",
            loaderDotActive = "#000000",
            loaderDotInactive = "#555555",
            toolCardBackground = "#FFFFFF",
            toolCardBorder = "#DDDDDD",
            toolCardTitle = "#111111",
            toolCardBody = "#333333"
        ),
        dark = ChatThemePalette(
            screenBackground = "#000000",
            titleText = "#EEEEEE",
            statusText = "#BBBBBB",
            composerBackground = "#111111",
            composerText = "#EEEEEE",
            composerPlaceholder = "#888888",
            userBubbleBackground = "#003366",
            userBubbleText = "#DDEEFF",
            assistantBubbleBackground = "#222222",
            assistantBubbleText = "#EEEEEE",
            loaderBubbleBackground = "#333333",
            loaderDotActive = "#FFFFFF",
            loaderDotInactive = "#999999",
            toolCardBackground = "#111111",
            toolCardBorder = "#444444",
            toolCardTitle = "#F0F0F0",
            toolCardBody = "#AAAAAA"
        )
    )
}
