package com.voicerooms.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VidorPurple = Color(0xFF7C3AED)
private val VidorDeepPurple = Color(0xFF3B0B72)
private val VidorDark = Color(0xFF15081F)
private val VidorSurface = Color(0xFF22112D)
private val VidorSoftSurface = Color(0xFF2C163C)
private val VidorGlow = Color(0xFFE8D8FF)
private val VidorText = Color(0xFFF5EEFF)
private val VidorMuted = Color(0xFFE6D0FF)

private val DarkColorScheme = darkColorScheme(
    primary = VidorPurple,
    onPrimary = VidorText,
    primaryContainer = VidorDeepPurple,
    onPrimaryContainer = VidorText,
    secondary = Color(0xFFA78BFA),
    onSecondary = VidorText,
    tertiary = VidorGlow,
    onTertiary = VidorDark,
    background = VidorDark,
    onBackground = VidorText,
    surface = VidorSurface,
    onSurface = VidorText,
    surfaceVariant = VidorSoftSurface,
    onSurfaceVariant = VidorMuted,
    outline = Color(0xFF8B5CF6)
)

@Composable
fun VoiceRoomsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
