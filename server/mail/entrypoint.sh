#!/bin/bash
# Настраивает Postfix + OpenDKIM при каждом запуске контейнера.
# Принимает письма только из внутренней сети Docker (наружу порт 25 не открыт)
# и отправляет их получателям напрямую, подписав DKIM-ключом домена.
set -euo pipefail

: "${MAIL_DOMAIN:?Задай MAIL_DOMAIN}"
SELECTOR="${DKIM_SELECTOR:-mail}"
KEYS=/etc/opendkim/keys
PRIVATE="$KEYS/$SELECTOR.private"

# Ключ DKIM создаётся один раз и хранится в томе, чтобы запись в DNS не менялась.
mkdir -p "$KEYS"
if [ ! -f "$PRIVATE" ]; then
  echo "Создаю DKIM-ключ для $MAIL_DOMAIN"
  openssl genrsa -out "$PRIVATE" 2048 2>/dev/null
fi
chown -R opendkim:opendkim "$KEYS"
chmod 700 "$KEYS"
chmod 600 "$PRIVATE"

# Значение TXT-записи для DNS (setup.sh показывает его пользователю).
PUBLIC="$(openssl rsa -in "$PRIVATE" -pubout -outform DER 2>/dev/null | base64 -w0)"
echo "v=DKIM1; k=rsa; p=$PUBLIC" > "$KEYS/dns-record.txt"
chmod 644 "$KEYS/dns-record.txt"

PRIVATE_NETS="127.0.0.0/8 10.0.0.0/8 172.16.0.0/12 192.168.0.0/16"

cat > /etc/opendkim.conf <<CONF
Mode                s
Domain              $MAIL_DOMAIN
Selector            $SELECTOR
KeyFile             $PRIVATE
InternalHosts       ${PRIVATE_NETS// /,}
Canonicalization    relaxed/simple
OversignHeaders     From
Socket              inet:8891@127.0.0.1
UserID              opendkim
Background          no
Syslog              no
CONF

postconf -e \
  "myhostname = $MAIL_DOMAIN" \
  "mydomain = $MAIL_DOMAIN" \
  "myorigin = $MAIL_DOMAIN" \
  "mydestination =" \
  "relay_domains =" \
  "local_recipient_maps =" \
  "local_transport = error:local delivery disabled" \
  "alias_maps =" \
  "alias_database =" \
  "inet_interfaces = all" \
  "inet_protocols = ipv4" \
  "mynetworks = $PRIVATE_NETS" \
  "smtpd_relay_restrictions = permit_mynetworks, reject" \
  "smtpd_recipient_restrictions = permit_mynetworks, reject" \
  "smtpd_tls_security_level = none" \
  "smtp_tls_security_level = may" \
  "smtp_tls_CAfile = /etc/ssl/certs/ca-certificates.crt" \
  "smtpd_milters = inet:127.0.0.1:8891" \
  "non_smtpd_milters = inet:127.0.0.1:8891" \
  "milter_default_action = tempfail" \
  "transport_maps = inline:{ $MAIL_DOMAIN = discard: }" \
  "maximal_queue_lifetime = 1h" \
  "bounce_queue_lifetime = 1h" \
  "message_size_limit = 1048576" \
  "maillog_file = /dev/stdout"
# Без chroot: иначе Postfix не видит DNS внутри Docker.
postconf -F '*/*/chroot = n'
echo "$MAIL_DOMAIN" > /etc/mailname

opendkim -f -x /etc/opendkim.conf &
DKIM_PID=$!
postfix start-fg &
POSTFIX_PID=$!

trap 'postfix stop >/dev/null 2>&1 || true; kill "$DKIM_PID" 2>/dev/null || true; exit 0' TERM INT
# Если один из процессов упал, выходим: Docker перезапустит контейнер.
wait -n "$DKIM_PID" "$POSTFIX_PID"
echo "OpenDKIM или Postfix остановился, перезапуск" >&2
exit 1
