package app.morsegram.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.morsegram.AppGraph
import app.morsegram.data.User
import kotlinx.coroutines.delay

/** Ищет людей, пока человек печатает: ждём 300 мс тишины, чтобы не дёргать сервер на каждую букву. */
@Composable
fun rememberPeopleSearch(query: String): List<User> {
    var results by remember { mutableStateOf(emptyList<User>()) }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        results = runCatching { AppGraph.people.search(query) }.getOrDefault(emptyList())
    }
    return results
}
