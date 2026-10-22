package app.morsegram.ui.auth

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import app.morsegram.ui.components.Entrance
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseTextField
import app.morsegram.ui.components.PasswordField
import app.morsegram.AppGraph
import app.morsegram.ui.errorText
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onCodeSent: (email: String) -> Unit, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val passwordError = if (password.isNotEmpty() && !isValidPassword(password)) {
        "Минимум $MIN_PASSWORD_LENGTH символов"
    } else {
        null
    }
    val repeatError = if (repeat.isNotEmpty() && repeat != password) "Пароли не совпадают" else null
    val canSubmit = isValidEmail(email) && isValidPassword(password) && repeat == password

    fun submit() = scope.launch {
        error = null
        sending = true
        try {
            AppGraph.account.register(email.trim(), password)
            sending = false
            onCodeSent(email.trim())
        } catch (e: Exception) {
            error = errorText(e)
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
        ErrorMessage(error)
        Entrance(delayMillis = 340) {
            GradientButton(
                text = "Зарегистрироваться",
                onClick = { submit() },
                enabled = canSubmit,
                loading = sending,
            )
        }
    }
}
