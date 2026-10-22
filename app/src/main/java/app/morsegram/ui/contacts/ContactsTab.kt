package app.morsegram.ui.contacts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.morsegram.AppGraph
import app.morsegram.ui.components.SearchField
import app.morsegram.ui.components.SectionTitle
import app.morsegram.ui.components.StaggeredRow
import app.morsegram.ui.components.UserRow
import app.morsegram.ui.components.rememberPeopleSearch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTab(onOpenChat: (userId: String) -> Unit, modifier: Modifier = Modifier) {
    val people = AppGraph.people
    val contacts by people.contacts.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()

    LaunchedEffect(people) { runCatching { people.refreshContacts() } }

    val contactIds = contacts.map { it.id }.toSet()
    val q = query.trim().removePrefix("@").lowercase()
    val shownContacts = if (q.isEmpty()) {
        contacts
    } else {
        contacts.filter { it.name.lowercase().contains(q) || it.nickname.lowercase().contains(q) }
    }
    val others = rememberPeopleSearch(query).filter { it.id !in contactIds }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(title = { Text("Контакты", fontWeight = FontWeight.Bold) }, scrollBehavior = scrollBehavior)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
        ) {
            item(key = "search") {
                SearchField(query, { query = it }, placeholder = "Найти по имени или @нику")
            }
            itemsIndexed(shownContacts, key = { _, user -> "contact-${user.id}" }) { index, user ->
                StaggeredRow(index, Modifier.animateItem()) {
                    UserRow(user, onClick = { onOpenChat(user.id) })
                }
            }
            if (others.isNotEmpty()) {
                item(key = "others-title") { SectionTitle("Найдено в Morsegram", Modifier.animateItem()) }
                itemsIndexed(others, key = { _, user -> "found-${user.id}" }) { index, user ->
                    StaggeredRow(index, Modifier.animateItem()) {
                        UserRow(
                            user = user,
                            onClick = { onOpenChat(user.id) },
                            trailing = {
                                AddContactButton(added = false) {
                                    scope.launch { runCatching { people.addContact(user.id) } }
                                }
                            },
                        )
                    }
                }
            }
            if (q.isEmpty() && contacts.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Пока пусто. Найди друзей по нику через поиск сверху.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddContactButton(added: Boolean, onAdd: () -> Unit) {
    FilledTonalIconButton(onClick = onAdd, enabled = !added) {
        AnimatedContent(
            targetState = added,
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            label = "addContact",
        ) { isAdded ->
            Icon(
                if (isAdded) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = if (isAdded) "В контактах" else "Добавить в контакты",
            )
        }
    }
}
