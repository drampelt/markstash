plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("plugin.serialization") version Versions.kotlin apply false
    id("com.github.ben-manes.versions") version Versions.versions
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(Dependencies.proguard)
        classpath(Dependencies.sqlDelightGradle)
        classpath(Dependencies.androidGradle)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
