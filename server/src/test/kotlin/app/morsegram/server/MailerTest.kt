package app.morsegram.server

import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MailerTest {

    @Test
    fun `свой почтовый сервер по умолчанию`() {
        val smtp = Config.fromEnv(mapOf("SMTP_HOST" to "mail", "MAIL_DOMAIN" to "morsegram.skvishik.fun")).smtp!!
        assertEquals(25, smtp.port)
        assertEquals("noreply@morsegram.skvishik.fun", smtp.from)
        assertFalse(smtp.authenticated)
    }

    @Test
    fun `пустые переменные из docker-compose не мешают`() {
        val env = mapOf(
            "SMTP_HOST" to "mail", "SMTP_PORT" to "", "SMTP_USER" to "", "MAIL_FROM" to "",
            "MAIL_DOMAIN" to "example.com",
        )
        val smtp = Config.fromEnv(env).smtp!!
        assertEquals(25, smtp.port)
        assertEquals("noreply@example.com", smtp.from)
    }

    @Test
    fun `внешний SMTP с логином`() {
        val env = mapOf("SMTP_HOST" to "smtp.gmail.com", "SMTP_PORT" to "587", "SMTP_USER" to "a@gmail.com")
        val smtp = Config.fromEnv(env).smtp!!
        assertEquals("a@gmail.com", smtp.from)
        assertTrue(smtp.authenticated)
    }

    @Test
    fun `письмо уходит на SMTP без пароля`() {
        val server = ServerSocket(0)
        val received = StringBuilder()
        val worker = thread { fakeSmtp(server, received) }

        val config = SmtpConfig("127.0.0.1", server.localPort, "", "", "noreply@example.com")
        SmtpMailer(config).sendCode("anna@example.com", "123456")
        worker.join(5000)
        server.close()

        val text = received.toString()
        assertTrue("MAIL FROM:<noreply@example.com>" in text, text)
        assertTrue("RCPT TO:<anna@example.com>" in text, text)
        assertFalse("AUTH" in text, text)
        assertTrue("123456" in text, text)
    }

    /** Простейший SMTP-сервер: принимает одно письмо и записывает весь разговор. */
    private fun fakeSmtp(server: ServerSocket, log: StringBuilder) {
        server.accept().use { socket ->
            val input = socket.getInputStream().bufferedReader()
            val output = socket.getOutputStream().bufferedWriter()
            fun reply(line: String) {
                output.write(line + "\r\n")
                output.flush()
            }
            reply("220 test")
            var inData = false
            while (true) {
                val line = input.readLine() ?: break
                synchronized(log) { log.appendLine(line) }
                when {
                    inData && line == "." -> { inData = false; reply("250 queued") }
                    inData -> Unit
                    line.startsWith("EHLO") -> reply("250 test")
                    line.startsWith("DATA") -> { inData = true; reply("354 go") }
                    line.startsWith("QUIT") -> { reply("221 bye"); break }
                    else -> reply("250 ok")
                }
            }
        }
    }
}
