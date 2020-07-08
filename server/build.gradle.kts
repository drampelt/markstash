plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version Versions.shadow
    id("com.squareup.sqldelight")
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

    implementation(project(":shared:api"))

    implementation(Dependencies.argon2)
    implementation(Dependencies.readability4j)
    implementation(Dependencies.koinKtor)
    implementation(Dependencies.ktorAuthJwt)
    implementation(Dependencies.ktorLocations)
    implementation(Dependencies.ktorSerialization)
    implementation(Dependencies.ktorServerCio)
    implementation(Dependencies.logbackClassic)
    implementation(Dependencies.selenium)
    implementation(Dependencies.sqlDelightSqlite)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }
}

sqldelight {
    database("Database") {
        packageName = "com.markstash.server.db"
        schemaOutputDirectory = file("src/main/sqldelight/databases")
    }
}
