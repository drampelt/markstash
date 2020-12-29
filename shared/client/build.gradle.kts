plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

version = "unspecified"

android {
    compileSdkVersion(Versions.androidCompileSdk)

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDir("src/androidMain/java")
            java.srcDir("src/androidMain/kotlin")
            res.srcDir("src/androidMain/res")
        }
    }
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

    ios()

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
            }
        }

        val jvmMain by getting {
            dependencies {
                api(Dependencies.ktorClientOkHttp)
            }
        }

        val androidMain by getting {
            dependencies {
                api(Dependencies.ktorClientOkHttp)
            }
        }

        val iosMain by getting {
            dependencies {
                api(Dependencies.ktorClientIos)
            }
        }
    }
}
