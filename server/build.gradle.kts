plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version Versions.shadow
}

group = "com.markstash.server"
version = "1.0-SNAPSHOT"

val main = "io.ktor.server.cio.EngineMain"

application {
    mainClassName = main
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(Dependencies.logbackClassic)
    implementation(Dependencies.ktorServerCio)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }
}
