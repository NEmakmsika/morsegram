package app.morsegram.ui.auth

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import app.morsegram.ui.components.Entrance
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseTextField
import app.morsegram.ui.components.PasswordField
import kotlinx.coroutines.delay

/** Пока сервера нет, подходит любая почта и любой пароль от 8 символов. */
@Composable
fun LoginScreen(onLoggedIn: (email: String) -> Unit, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var checking by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(checking) {
        if (checking) {
            delay(900)
            onLoggedIn(email.trim())
            checking = false
        }
    }

    AuthForm(
        title = "Вход",
        subtitle = "Почта и пароль, которые ты указал при регистрации.",
        onBack = onBack,
    ) {
        Entrance(delayMillis = 160) {
            MorseTextField(
                value = email,
                onValueChange = { email = it },
                label = "Почта",
                icon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
        }
        Entrance(delayMillis = 220) {
            PasswordField(password, { password = it }, "Пароль")
        }
        Entrance(delayMillis = 280) {
            GradientButton(
                text = "Войти",
                onClick = { checking = true },
                enabled = isValidEmail(email) && isValidPassword(password),
                loading = checking,
            )
        }
    }
}
