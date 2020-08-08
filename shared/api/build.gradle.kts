plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

version = "unspecified"

android {
    compileSdkVersion(Versions.androidCompileSdk)
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = Versions.jvm
            }
        }
    }

    js {
        browser()
    }

    android {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = Versions.jvm
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Dependencies.kotlinSerialization)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }

        val androidMain by getting {
            dependencies {
            }
        }
    }
}
