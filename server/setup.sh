#!/usr/bin/env bash
# Установка сервера Morsegram на чистый Ubuntu одной командой:
#   bash <(curl -fsSL https://raw.githubusercontent.com/NEmakmsika/morsegram/main/server/setup.sh)
# Скрипт спросит домен и почту, сам сгенерирует пароль базы и запустит всё в Docker.
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
  read -r -p "Gmail для отправки кодов (пусто — без писем, коды в логе): " SMTP_USER </dev/tty
  SMTP_PASSWORD=""
  if [ -n "$SMTP_USER" ]; then
    read -r -s -p "Пароль приложения Gmail (16 букв, ввод не отображается): " SMTP_PASSWORD </dev/tty
    echo
    SMTP_PASSWORD="${SMTP_PASSWORD// /}"
  fi

  umask 077
  cat > .env <<ENV
DOMAIN=$DOMAIN
DATABASE_PASSWORD=$(openssl rand -hex 24)
SMTP_HOST=$([ -n "$SMTP_USER" ] && echo smtp.gmail.com)
SMTP_PORT=587
SMTP_USER=$SMTP_USER
SMTP_PASSWORD=$SMTP_PASSWORD
MAIL_FROM=$SMTP_USER
ENV
  echo "-> Настройки сохранены в $DIR/server/.env (доступ только у root)"
else
  echo "-> Использую существующий $DIR/server/.env"
fi

# Открываем порты для HTTPS, если включён файрвол.
if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  ufw allow 80/tcp >/dev/null
  ufw allow 443/tcp >/dev/null
fi

echo "-> Собираю и запускаю (первый раз — несколько минут)"
docker compose up -d --build

DOMAIN_NOW="$(grep '^DOMAIN=' .env | cut -d= -f2)"
echo "-> Жду, пока сервер ответит на https://$DOMAIN_NOW/health"
for _ in $(seq 1 60); do
  if curl -fsS "https://$DOMAIN_NOW/health" >/dev/null 2>&1; then
    echo
    echo "== Готово! Сервер работает: https://$DOMAIN_NOW =="
    exit 0
  fi
  sleep 5
done

echo
echo "Сервер запущен, но по HTTPS пока не отвечает."
echo "Проверь, что DNS-запись $DOMAIN_NOW указывает на IP этого сервера: $(curl -fsS https://api.ipify.org || echo '?')"
echo "Логи: cd $DIR/server && docker compose logs --tail 50"
exit 1
