package app.morsegram.data

import app.morsegram.AppGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Демо-режим без сервера: подходит любой код и пароль, люди — из [FakeChatRepository]. */
class DemoAccountRepository(private val settings: SettingsRepository) : AccountRepository {
    override suspend fun register(email: String, password: String) = delay(700)

    override suspend fun verify(email: String, code: String) {
        delay(600)
        settings.logIn(email)
    }

    override suspend fun resend(email: String) = delay(400)

    override suspend fun login(email: String, password: String) {
        delay(700)
        settings.logIn(email)
    }

    override suspend fun updateProfile(displayName: String, nickname: String, bio: String) {
        settings.updateProfile(displayName, nickname, bio)
    }

    override suspend fun logout() = settings.logOut()
}

class DemoPeopleRepository : PeopleRepository {
    override val contacts: StateFlow<List<User>> = FakeChatRepository.contacts
        .map { ids -> FakeChatRepository.users.filter { it.id in ids }.sortedBy { it.name } }
        .stateIn(AppGraph.scope, SharingStarted.Eagerly, emptyList())

    override suspend fun refreshContacts() = Unit

    override suspend fun search(query: String): List<User> = FakeChatRepository.search(query)

    override suspend fun addContact(userId: String) = FakeChatRepository.addContact(userId)

    override suspend fun removeContact(userId: String) = FakeChatRepository.removeContact(userId)

    override fun cached(userId: String): User? = FakeChatRepository.user(userId)
}

