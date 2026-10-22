package app.morsegram.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Night,
    secondary = Violet,
    onSecondary = Color.White,
    background = Night,
    onBackground = Color.White,
    surface = Night,
    onSurface = Color.White,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = Color(0xFFB4BCD6),
    surfaceContainer = NightSurface,
    outline = Color(0xFF3A4566),
)

private val LightColors = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    secondary = Cyan,
    onSecondary = Ink,
    background = Snow,
    onBackground = Ink,
    surface = Snow,
    onSurface = Ink,
    surfaceVariant = SnowSurfaceHigh,
    onSurfaceVariant = Color(0xFF4B5563),
    surfaceContainer = Color.White,
    outline = Color(0xFFC7CCDB),
)

@Composable
fun MorsegramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
