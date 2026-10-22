package app.morsegram.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main() {
    val config = Config.fromEnv()
    val log = LoggerFactory.getLogger("Morsegram")

    val db = Database.connect(config.databaseUrl, config.databaseUser, config.databasePassword)
    db.migrate()

    val mailer = config.smtp?.let(::SmtpMailer) ?: LogMailer().also {
        log.warn("SMTP_HOST не задан: письма не отправляются, коды пишутся в лог")
    }

    embeddedServer(Netty, port = config.port) { morsegram(db, mailer) }.start(wait = true)
}
