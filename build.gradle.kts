plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("plugin.serialization") version Versions.kotlin apply false
    id("com.github.ben-manes.versions") version Versions.versions
    id("com.starter.easylauncher") version Versions.easylauncher apply false
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("xml-apis:xml-apis:1.4.01") // Workaround for https://github.com/cashapp/sqldelight/issues/2058
        classpath(Dependencies.proguard)
        classpath(Dependencies.sqlDelightGradle)
        classpath(Dependencies.androidGradle)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx/")
    }
}
