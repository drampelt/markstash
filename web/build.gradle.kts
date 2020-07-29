plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

dependencies {
    implementation(project(":shared:api"))
    implementation(project(":shared:client"))
    implementation(project(":shared:js"))

    implementation(Dependencies.kotlinReactRouterDom)

    implementation(npm("react-router-dom", Versions.reactRouter))
    implementation(npm("react-modal", Versions.reactModal))
    implementation(npm("js-cookie", Versions.jsCookie))
    implementation(npm("snapsvg", Versions.snapsvg)) // Required for muya
    implementation(npm("marktext", Versions.markText))
}

kotlin.js {
    browser {
        // See https://kotlinlang.org/docs/reference/javascript-dce.html#known-issue-dce-and-ktor
        dceTask {
            keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
        }
    }

    binaries.executable()
}
