package app.morsegram.server

/** Все настройки сервера берутся из переменных окружения (их задаёт docker-compose). */
data class Config(
    val port: Int,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val smtp: SmtpConfig?,
) {
    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): Config = Config(
            port = env["PORT"]?.toInt() ?: 8080,
            databaseUrl = env["DATABASE_URL"] ?: "jdbc:postgresql://localhost:5432/morsegram",
            databaseUser = env["DATABASE_USER"] ?: "morsegram",
            databasePassword = env["DATABASE_PASSWORD"] ?: "morsegram",
            smtp = env["SMTP_HOST"]?.takeIf { it.isNotBlank() }?.let { host ->
                SmtpConfig(
                    host = host,
                    port = env["SMTP_PORT"]?.toInt() ?: 587,
                    user = env["SMTP_USER"].orEmpty(),
                    password = env["SMTP_PASSWORD"].orEmpty(),
                    from = env["MAIL_FROM"] ?: env["SMTP_USER"].orEmpty(),
                )
            },
        )
    }
}

data class SmtpConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val from: String,
)
