plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("plugin.serialization") version Versions.kotlin apply false
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(Dependencies.proguard)
        classpath(Dependencies.sqlDelightGradle)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
