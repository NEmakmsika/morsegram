package app.morsegram.data

import kotlinx.coroutines.flow.StateFlow

/** Аккаунт: регистрация, подтверждение почты, вход, профиль. */
interface AccountRepository {
    suspend fun register(email: String, password: String)
    suspend fun verify(email: String, code: String)
    suspend fun resend(email: String)
    suspend fun login(email: String, password: String)
    suspend fun updateProfile(displayName: String, nickname: String, bio: String)
    suspend fun logout()
}

/** Люди: поиск по нику и контакты. */
interface PeopleRepository {
    val contacts: StateFlow<List<User>>
    suspend fun refreshContacts()
    suspend fun search(query: String): List<User>
    suspend fun addContact(userId: String)
    suspend fun removeContact(userId: String)

    /** Уже виденный пользователь (из поиска или контактов), чтобы показать имя в чате. */
    fun cached(userId: String): User?
}
