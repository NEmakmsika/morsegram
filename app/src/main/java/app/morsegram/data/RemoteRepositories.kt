package app.morsegram.data

import app.morsegram.data.remote.ApiClient
import app.morsegram.data.remote.ApiException
import app.morsegram.data.remote.MeResponse
import app.morsegram.data.remote.ProfileRequest
import app.morsegram.data.remote.PublicUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class RemoteAccountRepository(
    private val api: ApiClient,
    private val settings: SettingsRepository,
) : AccountRepository {

    override suspend fun register(email: String, password: String) = api.register(email, password)

    override suspend fun verify(email: String, code: String) {
        val session = api.verify(email, code)
        settings.logIn(session.user.email, session.token)
        saveProfile(session.user)
    }

    override suspend fun resend(email: String) = api.resend(email)

    override suspend fun login(email: String, password: String) {
        val session = api.login(email, password)
        settings.logIn(session.user.email, session.token)
        saveProfile(session.user)
    }

    override suspend fun updateProfile(displayName: String, nickname: String, bio: String) {
        saveProfile(api.updateProfile(ProfileRequest(displayName, nickname, bio)))
    }

    override suspend fun logout() {
        // Даже если сервер недоступен, на телефоне всё равно выходим.
        try {
            api.logout()
        } catch (_: ApiException) {
        }
        settings.logOut()
    }

    private suspend fun saveProfile(me: MeResponse) =
        settings.updateProfile(me.displayName, me.nickname.orEmpty(), me.bio)
}

class RemotePeopleRepository(private val api: ApiClient) : PeopleRepository {

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    override val contacts: StateFlow<List<User>> = _contacts.asStateFlow()

    private val cache = ConcurrentHashMap<String, User>()

    override suspend fun refreshContacts() {
        _contacts.value = api.contacts().map { it.toUser() }
    }

    override suspend fun search(query: String): List<User> {
        if (query.trim().removePrefix("@").length < 2) return emptyList()
        return api.search(query).map { it.toUser() }
    }

    override suspend fun addContact(userId: String) {
        api.addContact(userId.toLong())
        refreshContacts()
    }

    override suspend fun removeContact(userId: String) {
        api.removeContact(userId.toLong())
        refreshContacts()
    }

    override fun cached(userId: String): User? = cache[userId]

    private fun PublicUser.toUser() = User(
        id = id.toString(),
        name = displayName.ifBlank { nickname ?: "Без имени" },
        nickname = nickname.orEmpty(),
    ).also { cache[it.id] = it }
}
