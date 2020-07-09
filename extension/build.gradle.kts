plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation(project(":shared:api"))
    implementation(project(":shared:client"))
    implementation(project(":shared:js"))

    implementation(Dependencies.kotlinCoroutinesJs)
    implementation(Dependencies.kotlinReact)
    implementation(Dependencies.kotlinReactDom)

    implementation(npm("webextension-polyfill", Versions.webextensionPolyfill))
    implementation(npm("react", Versions.react))
    implementation(npm("react-dom", Versions.react))
}

kotlin.target.browser {
    // See https://kotlinlang.org/docs/reference/javascript-dce.html#known-issue-dce-and-ktor
    dceTask {
        keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
    }

    webpackTask {
        sourceMaps = false
        // Makes development faster but causes errors unless you add 'unsafe-eval' to the extension CSP
//        mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.DEVELOPMENT
    }
}
