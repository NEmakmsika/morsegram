package app.morsegram.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.morsegram.ui.components.AnimatedBackground
import app.morsegram.ui.components.Entrance
import app.morsegram.ui.theme.BrandGradient
import kotlinx.coroutines.delay

/** Пока сервера нет, подходит любой код из 6 цифр. Как только введена последняя цифра — входим. */
@Composable
fun CodeScreen(
    email: String,
    onCodeConfirmed: () -> Unit,
    onChangeEmail: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    val checking = isValidCode(code)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(checking) {
        if (checking) {
            delay(800)
            onCodeConfirmed()
        }
    }

    AnimatedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Entrance(delayMillis = 0) {
                Text(
                    "Код из письма",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(8.dp))
            Entrance(delayMillis = 100) {
                Text(
                    "Отправили на $email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(32.dp))
            Entrance(delayMillis = 200) {
                BasicTextField(
                    value = code,
                    onValueChange = { input ->
                        if (!checking) code = input.filter { it.isDigit() }.take(CODE_LENGTH)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.focusRequester(focusRequester),
                    decorationBox = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(CODE_LENGTH) { index ->
                                CodeCell(
                                    digit = code.getOrNull(index),
                                    active = index == code.length && !checking,
                                )
                            }
                        }
                    },
                )
            }
            Spacer(Modifier.height(32.dp))
            Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = checking,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { it }) togetherWith (fadeOut() + slideOutVertically { -it })
                    },
                    label = "codeFooter",
                ) { isChecking ->
                    if (isChecking) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(12.dp))
                            Text("Проверяем код…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        TextButton(onClick = onChangeEmail) { Text("Другая почта") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeCell(digit: Char?, active: Boolean) {
    val filled = digit != null
    val borderColor by animateColorAsState(
        when {
            active -> MaterialTheme.colorScheme.primary
            filled -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outline
        },
        label = "cellBorder",
    )
    val scale by animateFloatAsState(
        targetValue = if (filled) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
        label = "cellScale",
    )
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .border(if (active) 2.dp else 1.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = digit,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 2 }) togetherWith fadeOut()
            },
            label = "cellDigit",
        ) { value ->
            if (value != null) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else if (active) {
                Box(
                    Modifier
                        .size(width = 2.dp, height = 24.dp)
                        .background(BrandGradient),
                )
            }
        }
    }
}
