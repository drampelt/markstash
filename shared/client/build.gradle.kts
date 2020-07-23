plugins {
    kotlin("multiplatform")
}

version = "unspecified"

kotlin {
    js {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

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
                implementation(kotlin("stdlib-js"))

                api(Dependencies.ktorClientJs)
                api(Dependencies.ktorClientSerializationJs)

                // Required for ktor client
                api(npm("bufferutil"))
                api(npm("utf-8-validate"))
                api(npm("abort-controller"))
                api(npm("text-encoding"))
                api(npm("fs"))
            }
        }
    }
}
