package app.morsegram.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeChatRepositoryTest {

    @Test
    fun searchFindsByNicknameWithOrWithoutAt() {
        assertEquals(listOf("max"), FakeChatRepository.search("@max_morse").map { it.id })
        assertEquals(listOf("max"), FakeChatRepository.search("max_m").map { it.id })
    }

    @Test
    fun searchFindsByNameIgnoringCase() {
        assertEquals(listOf("lena"), FakeChatRepository.search("лена").map { it.id })
    }

    @Test
    fun emptyQueryFindsNobody() {
        assertTrue(FakeChatRepository.search("  ").isEmpty())
    }
}
