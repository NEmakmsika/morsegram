package app.morsegram.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun acceptsNormalEmail() {
        assertTrue(isValidEmail("anna@example.com"))
        assertTrue(isValidEmail("  anna@example.com  "))
    }

    @Test
    fun rejectsBrokenEmail() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("anna"))
        assertFalse(isValidEmail("anna@"))
        assertFalse(isValidEmail("anna@example"))
        assertFalse(isValidEmail("an na@example.com"))
    }

    @Test
    fun codeMustBeSixDigits() {
        assertTrue(isValidCode("123456"))
        assertFalse(isValidCode("12345"))
        assertFalse(isValidCode("1234567"))
        assertFalse(isValidCode("12a456"))
    }
}
