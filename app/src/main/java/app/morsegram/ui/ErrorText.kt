package app.morsegram.ui

import app.morsegram.data.remote.ApiException

/** Понятный текст ошибки для человека вместо кода от сервера. */
fun errorText(e: Throwable): String = when ((e as? ApiException)?.code) {
    "network" -> "Нет связи с сервером. Проверь интернет."
    "email_taken" -> "Эта почта уже зарегистрирована. Попробуй войти."
    "invalid_email" -> "Похоже, в почте ошибка."
    "invalid_password" -> "Пароль должен быть от 8 символов."
    "invalid_code" -> "Неверный код. Проверь письмо."
    "code_expired" -> "Код устарел или попыток больше нет. Запроси новый."
    "too_soon" -> "Новый код можно запросить через минуту."
    "invalid_credentials" -> "Неверная почта или пароль."
    "not_verified" -> "Почта ещё не подтверждена."
    "nickname_taken" -> "Этот ник уже занят."
    "invalid_nickname" -> "Ник: латиница, цифры и _, от 5 до 32 символов."
    "invalid_name" -> "Укажи имя."
    "unauthorized" -> "Сессия устарела, войди заново."
    else -> if (e.message?.startsWith("http_429") == true) {
        "Слишком много попыток. Подожди минуту."
    } else {
        "Что-то пошло не так. Попробуй ещё раз."
    }
}
