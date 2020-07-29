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
    }
}
