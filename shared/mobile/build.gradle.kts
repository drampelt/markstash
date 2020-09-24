import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = "0.1"

android {
    compileSdkVersion(Versions.androidCompileSdk)
}

kotlin {
    android {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = Versions.jvm
            }
        }
    }

    ios {
        binaries {
            all {
                if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Framework) {
                    export(project(":shared:client"))
                    export(project(":shared:api"))
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared:client"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
            }
        }
    }

    cocoapods {
        summary = "Shared module"
        homepage = "https://markstash.com"

        frameworkName = "shared"
    }
}
