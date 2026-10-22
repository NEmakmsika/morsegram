package app.morsegram.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Пока сервера нет, подходит любой код из 6 цифр. */
@Composable
fun CodeScreen(
    email: String,
    onCodeConfirmed: () -> Unit,
    onChangeEmail: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Код из письма", style = MaterialTheme.typography.headlineMedium)
            Text("Мы отправили код на $email")
            OutlinedTextField(
                value = code,
                onValueChange = { input -> code = input.filter { it.isDigit() }.take(CODE_LENGTH) },
                label = { Text("Код") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onCodeConfirmed,
                enabled = isValidCode(code),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Войти")
            }
            TextButton(onClick = onChangeEmail) {
                Text("Другая почта")
            }
        }
    }
}
