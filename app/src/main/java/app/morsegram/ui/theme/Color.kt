package app.morsegram.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF4FD1FF)
val Violet = Color(0xFF8B5CF6)
val Pink = Color(0xFFEC4899)

val Night = Color(0xFF0B1020)
val NightSurface = Color(0xFF141B2E)
val NightSurfaceHigh = Color(0xFF1E2740)

val Snow = Color(0xFFF6F7FB)
val SnowSurfaceHigh = Color(0xFFE9ECF5)
val Ink = Color(0xFF111827)

/** Фирменный градиент: им красим логотип, кнопки и свои сообщения. */
val BrandGradient = Brush.linearGradient(listOf(Cyan, Violet))

/** Цвета аватарок: имя собеседника всегда получает один и тот же градиент. */
val AvatarGradients = listOf(
    listOf(Cyan, Violet),
    listOf(Violet, Pink),
    listOf(Color(0xFF34D399), Cyan),
    listOf(Color(0xFFF59E0B), Pink),
)
