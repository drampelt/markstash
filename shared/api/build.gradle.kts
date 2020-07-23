plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

version = "unspecified"

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

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

                implementation(Dependencies.kotlinSerializationCommon)
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
                implementation(kotlin("stdlib-jdk8"))

                implementation(Dependencies.kotlinSerializationJvm)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))

                implementation(Dependencies.kotlinSerializationJs)
            }
        }
    }
}
