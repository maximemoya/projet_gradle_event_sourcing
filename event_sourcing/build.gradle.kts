plugins {
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "fr.maxime"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation(kotlin("reflect"))

    implementation(platform("org.http4k:http4k-bom:4.27.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-apache")
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-testing-approval")
    implementation("org.http4k:http4k-template-handlebars")
    implementation("org.http4k:http4k-multipart")
    implementation("org.slf4j:slf4j-nop:1.7.36") // Suppress log warnings from handlebars

    implementation("com.auth0:java-jwt:3.19.2")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.postgresql:postgresql:42.3.6")
    implementation("org.flywaydb:flyway-core:8.5.12")
//    implementation("com.zaxxer:HikariCP:5.0.1")
//    implementation("com.h2database:h2:2.1.210")

    implementation("org.apache.poi:poi-ooxml:4.1.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation ("org.jetbrains.kotlin:kotlin-test-common")

//    testImplementation(libs.junit)
//    testImplementation(libs.kotestFramework)
//    testImplementation(libs.kotestJunitXml)
//    testImplementation(libs.kotestAssertions)
//    testImplementation(libs.kotestArrow)
//    testImplementation(libs.kotestHttp4k)
//    testImplementation(libs.kotestJson)

//    testImplementation(kotlin("test"))
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}