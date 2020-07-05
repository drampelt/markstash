plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation(project(":shared:client"))

    implementation(Dependencies.kotlinCoroutinesJs)

    implementation(npm("webextension-polyfill"))
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
