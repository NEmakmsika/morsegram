package app.morsegram.server

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.util.Properties

fun interface Mailer {
    fun sendCode(to: String, code: String)
}

/** Настоящие письма через SMTP: свой почтовый сервер (mail/) или внешний вроде Gmail. */
class SmtpMailer(private val config: SmtpConfig) : Mailer {

    private val session: Session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port.toString())
            // Внутри Docker письмо идёт к своему серверу без пароля и шифрования,
            // внешнему SMTP — только с логином и по TLS.
            put("mail.smtp.auth", config.authenticated.toString())
            put("mail.smtp.starttls.enable", config.authenticated.toString())
            put("mail.smtp.starttls.required", config.authenticated.toString())
            put("mail.from", config.from)
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        },
    )

    override fun sendCode(to: String, code: String) {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.from, "Morsegram", "UTF-8"))
            setRecipient(Message.RecipientType.TO, InternetAddress(to))
            setSubject("Код для Morsegram: $code", "UTF-8")
            setText(
                "Твой код подтверждения: $code\n\n" +
                    "Он действует 10 минут. Если ты не регистрировался в Morsegram, просто удали это письмо.",
                "UTF-8",
            )
        }
        if (config.authenticated) {
            Transport.send(message, config.user, config.password)
        } else {
            Transport.send(message)
        }
    }
}

/** Для разработки без почты: код просто пишется в лог сервера. */
class LogMailer : Mailer {
    private val log = LoggerFactory.getLogger(LogMailer::class.java)

    override fun sendCode(to: String, code: String) {
        log.warn("SMTP не настроен. Код для {}: {}", to, code)
    }
}
