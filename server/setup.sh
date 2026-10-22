#!/usr/bin/env bash
# Установка сервера Morsegram на чистый Ubuntu одной командой:
#   bash <(curl -fsSL https://raw.githubusercontent.com/NEmakmsika/morsegram/main/server/setup.sh)
# Скрипт спросит домен, сам сгенерирует пароль базы, запустит всё в Docker
# и покажет записи DNS, которые нужны, чтобы письма с кодами не попадали в спам.
set -euo pipefail

REPO="https://github.com/NEmakmsika/morsegram.git"
BRANCH="${MORSEGRAM_BRANCH:-main}"
DIR="/opt/morsegram"

if [ "$(id -u)" -ne 0 ]; then
  echo "Запусти от root: sudo bash setup.sh" >&2
  exit 1
fi

echo "== Morsegram: установка сервера =="

if ! command -v docker >/dev/null 2>&1; then
  echo "-> Ставлю Docker"
  curl -fsSL https://get.docker.com | sh
fi
if ! command -v git >/dev/null 2>&1; then
  apt-get update -qq && apt-get install -y -qq git
fi

if [ -d "$DIR/.git" ]; then
  echo "-> Обновляю код"
  git -C "$DIR" fetch -q origin "$BRANCH"
  git -C "$DIR" checkout -q "$BRANCH"
  git -C "$DIR" reset -q --hard "origin/$BRANCH"
else
  echo "-> Скачиваю код"
  git clone -q --branch "$BRANCH" "$REPO" "$DIR"
fi
cd "$DIR/server"

if [ ! -f .env ]; then
  read -r -p "Домен сервера [morsegram.skvishik.fun]: " DOMAIN </dev/tty
  DOMAIN="${DOMAIN:-morsegram.skvishik.fun}"

  umask 077
  cat > .env <<ENV
DOMAIN=$DOMAIN
DATABASE_PASSWORD=$(openssl rand -hex 24)
ENV
  echo "-> Настройки сохранены в $DIR/server/.env (доступ только у root)"
else
  echo "-> Использую существующий $DIR/server/.env"
  # Старые настройки без внешней почты переводим на свой почтовый сервер.
  if grep -q '^SMTP_HOST=$' .env; then
    sed -i '/^SMTP_[A-Z]*=/d; /^MAIL_FROM=/d' .env
  fi
fi

DOMAIN="$(grep '^DOMAIN=' .env | cut -d= -f2)"
IP="$(curl -4 -fsS https://api.ipify.org || echo 'IP_СЕРВЕРА')"

# Многие хостинги закрывают исходящий порт 25 от спама. Без него свой сервер не сможет отправить письма.
if timeout 8 bash -c 'exec 3<>/dev/tcp/gmail-smtp-in.l.google.com/25' 2>/dev/null; then
  echo "-> Исходящий порт 25 открыт, свой почтовый сервер сможет отправлять письма"
  PORT25=open
else
  echo "!! Исходящий порт 25 закрыт хостингом: письма с кодами не уйдут."
  echo "!! Напиши в поддержку хостинга: «Откройте, пожалуйста, исходящий порт 25 для $IP»."
  PORT25=closed
fi

# Открываем порты для HTTPS, если включён файрвол.
if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  ufw allow 80/tcp >/dev/null
  ufw allow 443/tcp >/dev/null
fi

echo "-> Собираю и запускаю (первый раз — несколько минут)"
docker compose up -d --build

print_mail_dns() {
  local dkim="" zone="${DOMAIN#*.}" sub="${DOMAIN%%.*}"
  for _ in $(seq 1 20); do
    dkim="$(docker compose exec -T mail cat /etc/opendkim/keys/dns-record.txt 2>/dev/null || true)"
    [ -n "$dkim" ] && break
    sleep 2
  done
  {
    echo "== Записи DNS для почты =="
    echo "Добавь их у регистратора домена $zone, там же, где запись A для $DOMAIN."
    echo "Имя указано полностью; многие регистраторы просят только часть до .$zone"
    echo
    echo "1) Тип TXT  имя: $DOMAIN  ($sub)"
    echo "   значение: v=spf1 ip4:$IP -all"
    echo
    echo "2) Тип TXT  имя: mail._domainkey.$DOMAIN  (mail._domainkey.$sub)"
    echo "   значение: ${dkim:-не удалось получить, запусти скрипт ещё раз}"
    echo
    echo "3) Тип TXT  имя: _dmarc.$DOMAIN  (_dmarc.$sub)"
    echo "   значение: v=DMARC1; p=quarantine; adkim=s; aspf=s"
    echo
    echo "4) В панели хостинга: обратная запись (PTR, rDNS) для $IP -> $DOMAIN"
    echo
    echo "Проверить почту: отправь код на адрес с mail-tester.com и посмотри оценку."
  } | tee mail-dns.txt
  echo "(записи сохранены в $DIR/server/mail-dns.txt)"
}

echo "-> Жду, пока сервер ответит на https://$DOMAIN/health"
OK=no
for _ in $(seq 1 60); do
  if curl -fsS "https://$DOMAIN/health" >/dev/null 2>&1; then
    OK=yes
    break
  fi
  sleep 5
done

echo
print_mail_dns
echo
if [ "$PORT25" = closed ]; then
  echo "!! Не забудь: исходящий порт 25 закрыт, попроси хостинг его открыть."
fi

if [ "$OK" = yes ]; then
  echo "== Готово! Сервер работает: https://$DOMAIN =="
  exit 0
fi

echo "Сервер запущен, но по HTTPS пока не отвечает."
echo "Проверь, что DNS-запись $DOMAIN указывает на IP этого сервера: $IP"
echo "Логи: cd $DIR/server && docker compose logs --tail 50"
exit 1
