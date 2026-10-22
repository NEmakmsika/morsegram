# Свой почтовый сервер, который только отправляет письма с кодами.
# Postfix отправляет, OpenDKIM подписывает письма ключом домена, чтобы они не попадали в спам.
FROM debian:bookworm-slim

RUN echo "postfix postfix/main_mailer_type string 'Internet Site'" | debconf-set-selections \
 && echo "postfix postfix/mailname string localhost" | debconf-set-selections \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      postfix opendkim openssl ca-certificates \
 && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 25
ENTRYPOINT ["/entrypoint.sh"]
