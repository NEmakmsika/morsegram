plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("io.ktor.plugin") version "3.5.2"
}

group = "app.morsegram"
version = "0.1.0"

application {
    mainClass.set("app.morsegram.server.MainKt")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

val ktor = "3.5.2"

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-server-auth:$ktor")
    implementation("io.ktor:ktor-server-status-pages:$ktor")
    implementation("io.ktor:ktor-server-rate-limit:$ktor")
    implementation("io.ktor:ktor-server-call-logging:$ktor")

    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.eclipse.angus:angus-mail:2.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation("io.ktor:ktor-server-test-host:$ktor")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
