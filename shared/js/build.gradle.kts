plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

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

    // Muya Dependencies
    api(npm("snapsvg", Versions.snapsvg))
    api(npm("marktext", Versions.markText))

    api(npm("css-loader", Versions.cssLoader))
    api(npm("style-loader", Versions.styleLoader))
    api(npm("url-loader", Versions.urlLoader))
    api(npm("file-loader", Versions.fileLoader))
    api(npm("svg-sprite-loader", Versions.svgSpriteLoader))
    api(npm("postcss-loader", Versions.postcssLoader))
    api(npm("postcss-import", Versions.postcssImport))
    api(npm("postcss-nested", Versions.postcssNested))
    api(npm("tailwindcss", Versions.tailwindcss))
    api(npm("@tailwindcss/ui", Versions.tailwindUi))
    api(npm("autoprefixer", Versions.autoprefixer))
    api(npm("cssnano", Versions.cssnano))
    api(npm("babel-loader", Versions.babelLoader))
    api(npm("@babel/core", Versions.babel))
    api(npm("@babel/plugin-proposal-class-properties", Versions.babelPluginClassProperties))
    api(npm("@babel/preset-env", Versions.babelPresetEnv))
}

kotlin.target.browser { }
