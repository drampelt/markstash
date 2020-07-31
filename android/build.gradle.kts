plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(Versions.androidCompileSdk)

    defaultConfig {
        applicationId = "com.markstash.android"
        minSdkVersion(Versions.androidMinSdk)
        targetSdkVersion(Versions.androidTargetSdk)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = Versions.jvm
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerVersion = Versions.kotlinCompose
        kotlinCompilerExtensionVersion = Versions.kotlinComposeExtension
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = Versions.jvm
        freeCompilerArgs = freeCompilerArgs + listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
}

repositories {
    google()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))

    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeFoundationLayout)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeTooling)
    implementation(Dependencies.composeTest)
    implementation(Dependencies.xAppCompat)
}
