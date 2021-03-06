object Versions {
    const val jvm = "1.8"
    const val kotlin = "1.4.21"
    const val kotlinComposeExtension = "1.0.0-alpha09"
    const val graal = "20.1.0"
    const val androidMinSdk = 21
    const val androidTargetSdk = 29
    const val androidCompileSdk = 29
    const val node = "12.18.3"

    // Gradle plugins
    const val shadow = "6.1.0"
    const val sqlDelight = "1.4.4"
    const val proguard = "7.0.1"
    const val androidGradle = "7.0.0-alpha02"
    const val versions = "0.29.0"
    const val easylauncher = "3.9.0"

    // Libraries
    const val accompanist = "0.4.1"
    const val autoprefixer = "9.8.4"
    const val babel = "7.2.0"
    const val babelLoader = "8.1.0"
    const val babelPluginClassProperties = "7.10.1"
    const val babelPresetEnv = "7.2.0"
    const val bcrypt = "0.9.0"
    const val browserMob = "2.1.5"
    const val coil = "0.13.0"
    const val coilIco = "0.1.1"
    const val compose = "1.0.0-alpha09"
    const val composeNavigation = "1.0.0-alpha04"
    const val cssLoader = "3.6.0"
    const val cssnano = "4.1.10"
    const val fileLoader = "6.0.0"
    const val jsCookie = "2.2.1"
    const val jwarc = "0.13.0"
    const val koin = "2.2.2"
    const val kotlinCoroutines = "1.4.2-native-mt"
    const val kotlinDatetime = "0.1.1"
    const val kotlinReact = "16.13.1-pre.111-kotlin-1.4.0"
    const val kotlinReactRouter = "5.1.2-pre.111-kotlin-1.4.0"
    const val kotlinSerialization = "1.0.1"
    const val ktor = "1.5.0"
    const val logback = "1.2.3"
    const val markText = "https://github.com/drampelt/marktext#markstash"
    const val multiplatformSettings = "0.7"
    const val postcssLoader = "3.0.0"
    const val postcssImport = "12.0.1"
    const val postcssNested = "4.2.3"
    const val react = "16.13.1"
    const val reactModal = "3.11.2"
    const val reactRouter = "5.1.2"
    const val readability4j = "1.0.5"
    const val selenium = "3.8.1"
    const val snapsvg = "0.5.1"
    const val sqlite = "3.21.0.1"
    const val styleLoader = "1.2.1"
    const val svgSpriteLoader = "5.0.0"
    const val tailwindcss = "1.4.6"
    const val tailwindUi = "0.3.1"
    const val tailwindUiReact = "0.1.1"
    const val touchicon = "0.9.0"
    const val urlLoader = "4.1.0"
    const val webextensionPolyfill = "0.6.0"
    const val xAppCompat = "1.1.0"
    const val xLifecyle = "2.2.0"
}

object Dependencies {
    const val accompanist = "dev.chrisbanes.accompanist:accompanist-coil:${Versions.accompanist}"
    const val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradle}"
    const val bcrypt = "at.favre.lib:bcrypt:${Versions.bcrypt}"
    const val browserMob = "net.lightbody.bmp:browsermob-core:2.1.5"
    const val coil = "io.coil-kt:coil:${Versions.coil}"
    const val coilIco = "com.github.drampelt:coil-ico:${Versions.coilIco}"
    const val coilSvg = "io.coil-kt:coil-svg:${Versions.coil}"
    const val composeFoundation = "androidx.compose.foundation:foundation:${Versions.compose}"
    const val composeFoundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val composeMaterial = "androidx.compose.material:material:${Versions.compose}"
    const val composeMaterialIcons = "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val composeNavigation = "androidx.navigation:navigation-compose:${Versions.composeNavigation}"
    const val composeTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val jwarc = "org.netpreserve:jwarc:${Versions.jwarc}"
    const val koinAndroid = "org.koin:koin-android:${Versions.koin}"
    const val koinAndroidxCompose = "org.koin:koin-androidx-compose:${Versions.koin}"
    const val koinKtor = "org.koin:koin-ktor:${Versions.koin}"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val kotlinDatetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinDatetime}"
    const val kotlinReact = "org.jetbrains:kotlin-react:${Versions.kotlinReact}"
    const val kotlinReactDom = "org.jetbrains:kotlin-react-dom:${Versions.kotlinReact}"
    const val kotlinReactRouterDom = "org.jetbrains:kotlin-react-router-dom:${Versions.kotlinReactRouter}"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerialization}"
    const val ktorAuthJwt = "io.ktor:ktor-auth-jwt:${Versions.ktor}"
    const val ktorClientCio = "io.ktor:ktor-client-cio:${Versions.ktor}"
    const val ktorClientCore = "io.ktor:ktor-client-core:${Versions.ktor}"
    const val ktorClientIos = "io.ktor:ktor-client-ios:${Versions.ktor}"
    const val ktorClientJs = "io.ktor:ktor-client-js:${Versions.ktor}"
    const val ktorClientOkHttp = "io.ktor:ktor-client-okhttp:${Versions.ktor}"
    const val ktorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
    const val ktorLocations = "io.ktor:ktor-locations:${Versions.ktor}"
    const val ktorSerialization = "io.ktor:ktor-serialization:${Versions.ktor}"
    const val ktorServerCio = "io.ktor:ktor-server-cio:${Versions.ktor}"
    const val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logback}"
    const val multiplatformSettings = "com.russhwolf:multiplatform-settings-no-arg:${Versions.multiplatformSettings}"
    const val proguard = "com.guardsquare:proguard-gradle:${Versions.proguard}"
    const val readability4j = "net.dankito.readability4j:readability4j:${Versions.readability4j}"
    const val selenium = "org.seleniumhq.selenium:selenium-java:${Versions.selenium}"
    const val sqlDelightGradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
    const val sqlDelightSqlite = "com.squareup.sqldelight:sqlite-driver:${Versions.sqlDelight}"
    const val sqlite = "org.xerial:sqlite-jdbc:${Versions.sqlite}"
    const val svm = "org.graalvm.nativeimage:svm:${Versions.graal}"
    const val touchicon = "net.mm2d:touchicon:${Versions.touchicon}"
    const val xAppCompat = "androidx.appcompat:appcompat:${Versions.xAppCompat}"
    const val xViewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.xLifecyle}"
}
