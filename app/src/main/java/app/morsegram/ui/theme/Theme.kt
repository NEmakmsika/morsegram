package app.morsegram.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.morsegram.data.ThemeMode

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

/** Плавно перетекаем из одной темы в другую, а не переключаемся рывком. */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    return copy(
        primary = primary.anim("primary"),
        secondary = secondary.anim("secondary"),
        background = background.anim("background"),
        onBackground = onBackground.anim("onBackground"),
        surface = surface.anim("surface"),
        onSurface = onSurface.anim("onSurface"),
        surfaceVariant = surfaceVariant.anim("surfaceVariant"),
        onSurfaceVariant = onSurfaceVariant.anim("onSurfaceVariant"),
        surfaceContainer = surfaceContainer.anim("surfaceContainer"),
        outline = outline.anim("outline"),
    )
}

@Composable
private fun Color.anim(label: String): Color = animateColorAsState(this, tween(450), label = label).value

/** «Чёрная» тема: настоящий чёрный фон, на AMOLED-экранах экономит батарею. */
private val BlackColors = DarkColors.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF16181F),
    surfaceContainer = Color(0xFF0C0D12),
    outline = Color(0xFF2A2F3D),
)

@Composable
fun MorsegramTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val colors = when (mode) {
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) DarkColors else LightColors
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.BLACK -> BlackColors
    }
    MaterialTheme(colorScheme = colors.animated(), content = content)
}
