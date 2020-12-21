plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.starter.easylauncher")
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
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
        kotlinCompilerVersion = Versions.kotlin
        kotlinCompilerExtensionVersion = Versions.kotlinComposeExtension
    }
}

easylauncher {
    buildTypes {
        create("debug") {
            filters(blueRibbonFilter())
        }
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = Versions.jvm
        freeCompilerArgs = freeCompilerArgs + listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check", "-Xjvm-default=compatibility")
    }
}

repositories {
    google()
    jcenter()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))

    implementation(project(":shared:api"))
    implementation(project(":shared:client"))
    implementation(project(":shared:mobile"))

    implementation(Dependencies.accompanist)
    implementation(Dependencies.coil)
    implementation(Dependencies.coilIco)
    implementation(Dependencies.coilSvg)
    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeFoundationLayout)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeMaterialIcons)
    implementation(Dependencies.composeNavigation)
    implementation(Dependencies.composeTooling)
    implementation(Dependencies.koinAndroid)
    implementation(Dependencies.koinAndroidxCompose)
    implementation(Dependencies.kotlinSerialization)
    implementation(Dependencies.touchicon)
    implementation(Dependencies.xAppCompat)
}
