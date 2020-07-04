plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation(npm("webextension-polyfill"))
}

kotlin.target.browser { }
