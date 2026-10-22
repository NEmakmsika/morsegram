# Morsegram

Android-мессенджер со сквозным шифрованием (Signal Protocol).

Сейчас это каркас приложения: экраны входа по почте, список чатов и чат.
Сервера и шифрования пока нет, сообщения живут только в памяти телефона.

## Стек

- Kotlin + Jetpack Compose
- libsignal (будет подключён на этапе 4)
- Сервер: Kotlin + Ktor + PostgreSQL (этап 3)

## Как собрать

Открыть папку в Android Studio и нажать Run.

Или без Android Studio: каждая сборка в GitHub Actions выкладывает готовый
файл `app-debug.apk`. Открой вкладку **Actions**, выбери последний запуск
и скачай `morsegram-debug-apk` внизу страницы.
