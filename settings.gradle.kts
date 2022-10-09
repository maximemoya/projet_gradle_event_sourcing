rootProject.name = "project_gradle_2"
include("event_sourcing")

val kotestVersion = "5.3.1"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            alias("arrowCore").to("io.arrow-kt:arrow-core:1.1.2")
            alias("kotlinxCoroutines").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
            alias("junit").to("org.junit.jupiter:junit-jupiter:5.8.2")
            alias("kotestFramework").to("io.kotest:kotest-runner-junit5:$kotestVersion")
            alias("kotestJunitXml").to("io.kotest:kotest-extensions-junitxml:$kotestVersion")
            alias("kotestAssertions").to("io.kotest:kotest-assertions-core:$kotestVersion")
            alias("kotestJson").to("io.kotest:kotest-assertions-json:$kotestVersion")
            alias("kotestArrow").to("io.kotest.extensions:kotest-assertions-arrow:1.2.5")
            alias("kotestHttp4k").to("org.http4k:http4k-testing-kotest:4.27.0.0")
        }
    }
}
