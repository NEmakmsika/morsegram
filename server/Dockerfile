FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./gradlew installDist --no-daemon -q

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 morsegram
WORKDIR /app
COPY --from=build /src/build/install/morsegram-server /app
USER morsegram
EXPOSE 8080
ENTRYPOINT ["/app/bin/morsegram-server"]
