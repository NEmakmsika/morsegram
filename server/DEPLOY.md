# Как запустить сервер Morsegram

Понадобится сервер (VPS) с Ubuntu 24.04 и 2 ГБ памяти и домен.
Письма с кодами отправляет свой почтовый сервер, он ставится вместе со всем остальным.

## Быстрый способ

1. Добавь у регистратора домена DNS-запись типа **A**: имя `morsegram`, значение — IP сервера
   (получится `morsegram.ТВОЙ_ДОМЕН`).
2. Подключись к серверу (`ssh root@IP_СЕРВЕРА`) и выполни:

```
bash <(curl -fsSL https://raw.githubusercontent.com/NEmakmsika/morsegram/main/server/setup.sh)
```

Скрипт сам поставит Docker, спросит домен, сгенерирует пароль базы и запустит сервер.
В конце он покажет 3 записи DNS для почты и подскажет про PTR (раздел 2 ниже).
3. Добавь эти записи у регистратора и PTR в панели хостинга.

Повторный запуск той же команды обновляет сервер до новой версии и снова показывает записи.

Ниже — то же самое вручную.

## 1. Домен

Телефон соединяется с сервером только по HTTPS, а для HTTPS нужен домен.
Бесплатный вариант — [duckdns.org](https://www.duckdns.org):

1. Войди через Google или GitHub.
2. Придумай имя, например `morsegram` → получится `morsegram.duckdns.org`.
3. В поле **current ip** впиши IP своего сервера и нажми **update ip**.

## 2. Почта для кодов

Письма уходят с адреса `noreply@ДОМЕН` (например, `noreply@morsegram.skvishik.fun`).
Отправляет их свой почтовый сервер в Docker (папка `mail/`: Postfix + подпись DKIM).
Он только отправляет: принимать почту и пересылать чужие письма он не умеет, порт 25 наружу не открыт.

Чтобы Gmail, Яндекс и Mail.ru не складывали письма в спам, нужны записи DNS.
`setup.sh` печатает их с готовыми значениями и сохраняет в `mail-dns.txt`:

| Тип | Имя | Значение |
|---|---|---|
| TXT | `ДОМЕН` | `v=spf1 ip4:IP_СЕРВЕРА -all` — письма с этого домена шлёт только наш сервер |
| TXT | `mail._domainkey.ДОМЕН` | `v=DKIM1; k=rsa; p=…` — открытый ключ для проверки подписи |
| TXT | `_dmarc.ДОМЕН` | `v=DMARC1; p=quarantine; adkim=s; aspf=s` — что делать с подделками |

И ещё **PTR** (обратная запись, rDNS): в панели хостинга для IP сервера укажи `ДОМЕН`.
Без неё Gmail часто отклоняет письма.

Важно: многие хостинги закрывают **исходящий порт 25**. `setup.sh` это проверяет;
если закрыт, напиши в поддержку хостинга и попроси открыть.

Проверка: на [mail-tester.com](https://www.mail-tester.com) возьми адрес, зарегистрируйся
в приложении на него и посмотри оценку. Хорошо — 9/10 и выше.

Если свой почтовый сервер не нужен, в `.env` можно указать внешний SMTP (`SMTP_HOST`,
`SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM`), например Gmail с паролем приложения.

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

В редакторе заполни `DOMAIN` и `DATABASE_PASSWORD`.
Пароль базы можно сгенерировать командой `openssl rand -hex 24`.
Сохранить: `Ctrl+O`, `Enter`, выйти: `Ctrl+X`.

## 5. Запустить

```
docker compose up -d --build
```

Первый запуск занимает несколько минут. Проверка — открой в браузере
`https://ТВОЙ_ДОМЕН/health`, должно показать `{"ok":true}`.

Значение DKIM для DNS: `docker compose exec mail cat /etc/opendkim/keys/dns-record.txt`.

## Обновить до новой версии

```
cd ~/morsegram && git pull && cd server && docker compose up -d --build
```

## Посмотреть логи

```
docker compose logs -f server
docker compose logs -f mail     # почта: ушло письмо или нет
```
