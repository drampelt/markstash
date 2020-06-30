object Versions {
    const val jvm = "1.8"
    const val kotlin = "1.3.72"

    // Gradle plugins
    const val shadow = "5.2.0"

    // Libraries
    const val koin = "2.1.5"
    const val ktor = "1.3.2"
    const val logback = "1.2.3"
}

object Dependencies {
    const val koinKtor = "org.koin:koin-ktor:${Versions.koin}"
    const val ktorServerCio = "io.ktor:ktor-server-cio:${Versions.ktor}"
    const val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logback}"
}
