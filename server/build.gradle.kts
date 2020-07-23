import proguard.gradle.ProGuardTask

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

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":shared:api"))

    implementation(Dependencies.argon2)
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

    named<JavaExec>("run") {
        args("-config=application.dev.conf")
    }

    register("minimizedJar", ProGuardTask::class.java) {
        dependsOn("shadowJar")

        injars("$buildDir/libs/server.jar")
        outjars("$buildDir/libs/server.min.jar")
        libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
        libraryjars("${System.getProperty("java.home")}/lib/jce.jar")

        configuration("proguard.pro")
    }
}

sqldelight {
    database("Database") {
        packageName = "com.markstash.server.db"
        schemaOutputDirectory = file("src/main/sqldelight/databases")
    }
}
