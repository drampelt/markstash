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

    // All dependencies have to specified in the shared module, even if they aren't used in some
    api(Dependencies.kotlinCoroutinesJs)
    api(Dependencies.kotlinReact)
    api(Dependencies.kotlinReactDom)
    api(Dependencies.kotlinReactRouterDom)

    api(npm("react", Versions.react))
    api(npm("react-dom", Versions.react))
    api(npm("react-router-dom", Versions.reactRouter))
    api(npm("js-cookie", Versions.jsCookie))
    api(npm("webextension-polyfill", Versions.webextensionPolyfill))

    api(npm("css-loader", Versions.cssLoader))
    api(npm("style-loader", Versions.styleLoader))
    api(npm("postcss-loader", Versions.postcssLoader))
    api(npm("postcss-import", Versions.postcssImport))
    api(npm("postcss-nested", Versions.postcssNested))
    api(npm("tailwindcss", Versions.tailwindcss))
    api(npm("autoprefixer", Versions.autoprefixer))
    api(npm("cssnano", Versions.cssnano))
}

kotlin.target.browser { }
