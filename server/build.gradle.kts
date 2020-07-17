plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version Versions.shadow
    id("com.squareup.sqldelight")
}

group = "com.markstash.server"
version = "1.0-SNAPSHOT"

val main = "com.markstash.server.ApplicationKt"

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
    implementation(Dependencies.ashot)
    implementation(Dependencies.browserMob)
    implementation(Dependencies.jwarc)
    implementation(Dependencies.koinKtor)
    implementation(Dependencies.ktorAuthJwt)
    implementation(Dependencies.ktorLocations)
    implementation(Dependencies.ktorSerialization)
    implementation(Dependencies.ktorServerCio)
    implementation(Dependencies.logbackClassic)
    implementation(Dependencies.readability4j)
    implementation(Dependencies.selenium)
    implementation(Dependencies.sqlDelightSqlite)
    implementation(Dependencies.sqlite)

    compileOnly(Dependencies.svm)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = Versions.jvm
    }

    shadowJar {
        dependsOn(":web:assemble")

        archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")

        into("/assets") {
            from(fileTree("${project(":web").buildDir}/distributions"))
            exclude("*.map", "css")
        }
    }
}

sqldelight {
    database("Database") {
        packageName = "com.markstash.server.db"
        schemaOutputDirectory = file("src/main/sqldelight/databases")
    }
}
