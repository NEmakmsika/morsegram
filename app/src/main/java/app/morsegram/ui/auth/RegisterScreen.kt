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

@Composable
fun RegisterScreen(onCodeSent: (email: String) -> Unit, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var sending by rememberSaveable { mutableStateOf(false) }

    val passwordError = if (password.isNotEmpty() && !isValidPassword(password)) {
        "Минимум $MIN_PASSWORD_LENGTH символов"
    } else {
        null
    }
    val repeatError = if (repeat.isNotEmpty() && repeat != password) "Пароли не совпадают" else null
    val canSubmit = isValidEmail(email) && isValidPassword(password) && repeat == password

    // Пока сервера нет, делаем вид, что письмо отправляется.
    LaunchedEffect(sending) {
        if (sending) {
            delay(900)
            onCodeSent(email.trim())
            sending = false
        }
    }

    AuthForm(
        title = "Регистрация",
        subtitle = "Придумай пароль — потом пришлём код на почту, чтобы подтвердить, что она твоя.",
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
            PasswordField(password, { password = it }, "Пароль", error = passwordError)
        }
        Entrance(delayMillis = 280) {
            PasswordField(repeat, { repeat = it }, "Повтори пароль", error = repeatError)
        }
        Entrance(delayMillis = 340) {
            GradientButton(
                text = "Зарегистрироваться",
                onClick = { sending = true },
                enabled = canSubmit,
                loading = sending,
            )
        }
    }
}
