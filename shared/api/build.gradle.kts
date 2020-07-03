plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

version = "unspecified"

repositories {
    jcenter()
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = Versions.jvm
            }
        }
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
    }
}
