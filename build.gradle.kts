plugins {
    kotlin("jvm") version Versions.kotlin apply false
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(Dependencies.sqlDelightGradle)
    }
}
