object Versions {
    const val jvm = "1.8"
    const val kotlin = "1.3.72"

    // Gradle plugins
    const val shadow = "5.2.0"
    const val sqlDelight = "1.4.0"

    // Libraries
    const val argon2 = "2.7"
    const val koin = "2.1.5"
    const val kotlinCoroutines = "1.3.7"
    const val kotlinReact = "16.13.1-pre.105-kotlin-1.3.72"
    const val kotlinSerialization = "0.20.0"
    const val ktor = "1.3.2"
    const val logback = "1.2.3"
    const val react = "16.13.1"
}

object Dependencies {
    const val argon2 = "de.mkammerer:argon2-jvm:${Versions.argon2}"
    const val koinKtor = "org.koin:koin-ktor:${Versions.koin}"
    const val kotlinCoroutinesCommon = "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Versions.kotlinCoroutines}"
    const val kotlinCoroutinesJs = "org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Versions.kotlinCoroutines}"
    const val kotlinReact = "org.jetbrains:kotlin-react:${Versions.kotlinReact}"
    const val kotlinReactDom = "org.jetbrains:kotlin-react-dom:${Versions.kotlinReact}"
    const val kotlinSerializationCommon = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Versions.kotlinSerialization}"
    const val kotlinSerializationJs = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${Versions.kotlinSerialization}"
    const val kotlinSerializationJvm = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinSerialization}"
    const val ktorAuthJwt = "io.ktor:ktor-auth-jwt:${Versions.ktor}"
    const val ktorClientCore = "io.ktor:ktor-client-core:${Versions.ktor}"
    const val ktorClientJs = "io.ktor:ktor-client-js:${Versions.ktor}"
    const val ktorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
    const val ktorClientSerializationJs = "io.ktor:ktor-client-serialization-js:${Versions.ktor}"
    const val ktorLocations = "io.ktor:ktor-locations:${Versions.ktor}"
    const val ktorSerialization = "io.ktor:ktor-serialization:${Versions.ktor}"
    const val ktorServerCio = "io.ktor:ktor-server-cio:${Versions.ktor}"
    const val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logback}"
    const val sqlDelightGradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
    const val sqlDelightSqlite = "com.squareup.sqldelight:sqlite-driver:${Versions.sqlDelight}"
}
