package app.morsegram.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import app.morsegram.ui.theme.Cyan
import app.morsegram.ui.theme.Violet
import kotlin.math.cos
import kotlin.math.sin

/** Фон с двумя мягкими цветными пятнами, которые медленно плавают. */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "background")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
        label = "angle",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val radius = size.maxDimension * 0.6f
                val first = Offset(
                    x = size.width * (0.2f + 0.15f * cos(angle)),
                    y = size.height * (0.2f + 0.1f * sin(angle)),
                )
                val second = Offset(
                    x = size.width * (0.85f - 0.15f * sin(angle)),
                    y = size.height * (0.8f + 0.1f * cos(angle)),
                )
                drawCircle(
                    Brush.radialGradient(listOf(Cyan.copy(alpha = 0.28f), Cyan.copy(alpha = 0f)), first, radius),
                    radius,
                    first,
                )
                drawCircle(
                    Brush.radialGradient(listOf(Violet.copy(alpha = 0.3f), Violet.copy(alpha = 0f)), second, radius),
                    radius,
                    second,
                )
            },
        content = content,
    )
}
