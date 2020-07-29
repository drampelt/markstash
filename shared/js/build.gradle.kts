plugins {
    id("org.jetbrains.kotlin.js")
}

version = "unspecified"

dependencies {
    api(project(":shared:api"))
    api(project(":shared:client"))

    api(Dependencies.kotlinCoroutines)
    api(Dependencies.kotlinReact)
    api(Dependencies.kotlinReactDom)

    api(npm("react", Versions.react))
    api(npm("react-dom", Versions.react))

    api(devNpm("css-loader", Versions.cssLoader))
    api(devNpm("style-loader", Versions.styleLoader))
    api(devNpm("url-loader", Versions.urlLoader))
    api(devNpm("file-loader", Versions.fileLoader))
    api(devNpm("svg-sprite-loader", Versions.svgSpriteLoader))
    api(devNpm("postcss-loader", Versions.postcssLoader))
    api(devNpm("postcss-import", Versions.postcssImport))
    api(devNpm("postcss-nested", Versions.postcssNested))
    api(devNpm("tailwindcss", Versions.tailwindcss))
    api(devNpm("@tailwindcss/ui", Versions.tailwindUi))
    api(devNpm("autoprefixer", Versions.autoprefixer))
    api(devNpm("cssnano", Versions.cssnano))
    api(devNpm("babel-loader", Versions.babelLoader))
    api(devNpm("@babel/core", Versions.babel))
    api(devNpm("@babel/plugin-proposal-class-properties", Versions.babelPluginClassProperties))
    api(devNpm("@babel/preset-env", Versions.babelPresetEnv))
}

kotlin.js {
    browser()
}
