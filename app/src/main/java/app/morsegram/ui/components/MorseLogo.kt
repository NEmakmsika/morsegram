package app.morsegram.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.morsegram.ui.theme.BrandGradient
import kotlin.math.abs

/** «MG» азбукой Морзе: −− −−· */
private val MorseMG = listOf(false, false, null, false, false, true)

/**
 * Логотип из точек и тире. По нему бежит световая волна,
 * как будто сигнал передаётся прямо сейчас.
 */
@Composable
fun MorseLogo(modifier: Modifier = Modifier, unit: Dp = 14.dp) {
    val transition = rememberInfiniteTransition(label = "morse")
    val wave by transition.animateFloat(
        initialValue = -1f,
        targetValue = MorseMG.size + 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "wave",
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(unit / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MorseMG.forEachIndexed { index, isDot ->
            if (isDot == null) {
                Box(Modifier.size(unit / 2))
            } else {
                val glow = 1f - abs(wave - index).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .graphicsLayer {
                            alpha = 0.45f + 0.55f * glow
                            scaleX = 1f + 0.15f * glow
                            scaleY = 1f + 0.15f * glow
                        }
                        .size(width = if (isDot) unit else unit * 3, height = unit)
                        .background(BrandGradient, if (isDot) CircleShape else RoundedCornerShape(unit / 2)),
                )
            }
        }
    }
}
