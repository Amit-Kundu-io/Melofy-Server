
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.amit_kundu_io"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.autoHeadResponse)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.routingOpenapi)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.swagger)
    implementation(libs.logback.classic)


    implementation(libs.exposed.core)
    implementation(libs.h2database.h2)
    implementation(libs.koin.ktor)
    implementation(libs.koin.loggerSlf4j)
    implementation(libs.postgresql)


    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)

    implementation("org.mindrot:jbcrypt:0.4")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("org.flywaydb:flyway-core:10.20.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.20.1")

    implementation("org.jetbrains.exposed:exposed-java-time:1.3.0")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)



    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.defaultHeaders)



    implementation("com.google.apis:google-api-services-drive:v3-rev20250506-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.30.1")
    implementation("com.google.http-client:google-http-client-gson:1.47.0")

    implementation("io.ktor:ktor-client-core:3.5.1")
    implementation("io.ktor:ktor-client-cio:3.5.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.5.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.1")
}
