# Как запустить сервер Morsegram

Понадобится сервер (VPS) с Ubuntu 24.04 и 2 ГБ памяти, домен и почта для писем с кодом.

## Быстрый способ

1. Добавь у регистратора домена DNS-запись типа **A**: имя `morsegram`, значение — IP сервера
   (получится `morsegram.ТВОЙ_ДОМЕН`).
2. Сделай пароль приложения Gmail (раздел 2 ниже).
3. Подключись к серверу (`ssh root@IP_СЕРВЕРА`) и выполни:

```
bash <(curl -fsSL https://raw.githubusercontent.com/NEmakmsika/morsegram/main/server/setup.sh)
```

Скрипт сам поставит Docker, спросит домен и почту, сгенерирует пароль базы и запустит сервер.
Повторный запуск той же команды обновляет сервер до новой версии.

Ниже — то же самое вручную.

## 1. Домен

Телефон соединяется с сервером только по HTTPS, а для HTTPS нужен домен.
Бесплатный вариант — [duckdns.org](https://www.duckdns.org):

1. Войди через Google или GitHub.
2. Придумай имя, например `morsegram` → получится `morsegram.duckdns.org`.
3. В поле **current ip** впиши IP своего сервера и нажми **update ip**.

## 2. Почта для кодов (Gmail)

1. Заведи отдельный Gmail, например `morsegram.app@gmail.com`.
2. Включи двухэтапную проверку: myaccount.google.com → Безопасность.
3. Там же создай **пароль приложения** (поиск по настройкам: «Пароли приложений»).
   Получится строка из 16 букв — это `SMTP_PASSWORD`. Никому её не отправляй.

## 3. Подключиться к серверу

На компьютере открой терминал (в Windows — PowerShell) и выполни:

```
ssh root@IP_СЕРВЕРА
```

## 4. Поставить Docker и скачать Morsegram

```
curl -fsSL https://get.docker.com | sh
git clone https://github.com/NEmakmsika/morsegram.git
cd morsegram/server
cp .env.example .env
nano .env
```

В редакторе заполни `DOMAIN`, `DATABASE_PASSWORD`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM`.
Пароль базы можно сгенерировать командой `openssl rand -hex 24`.
Сохранить: `Ctrl+O`, `Enter`, выйти: `Ctrl+X`.

## 5. Запустить

```
docker compose up -d --build
```

Первый запуск занимает несколько минут. Проверка — открой в браузере
`https://ТВОЙ_ДОМЕН/health`, должно показать `{"ok":true}`.

## Обновить до новой версии

```
cd ~/morsegram && git pull && cd server && docker compose up -d --build
```

## Посмотреть логи

```
docker compose logs -f server
```
