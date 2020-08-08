plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

version = "unspecified"

android {
    compileSdkVersion(Versions.androidCompileSdk)
}

kotlin {
    js {
        browser()
    }

    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = Versions.jvm
            }
        }
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
                api(project(":shared:api"))

                implementation(Dependencies.ktorClientCore)
                implementation(Dependencies.ktorClientSerialization)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {
            dependencies {
                api(Dependencies.ktorClientJs)
                api(Dependencies.ktorClientSerializationJs)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(Dependencies.ktorClientOkHttp)
                api(Dependencies.ktorClientSerializationJvm)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Dependencies.ktorClientOkHttp)
                api(Dependencies.ktorClientSerializationJvm)
            }
        }
    }
}
