config.module.rules.push({
    test: /muya\/.*\.js$/,
    exclude: /muya.*node_modules/,
    loader: 'babel-loader',
    options: {
        presets: [
            ['@babel/preset-env',
            {
                "targets": {
                    "browsers": [
                        "last 2 Chrome major versions",
                        "last 2 Firefox major versions",
                        "last 2 Safari major versions",
                        "last 2 Edge major versions",
                        "last 2 iOS major versions",
                        "last 2 ChromeAndroid major versions",
                    ],
                },
            }],
        ],
        plugins: [
            ['@babel/plugin-proposal-class-properties', { loose: true }],
        ],
    }
});

config.externals = Object.assign({}, config.externals, {
    'fs': 'fs',
});

if (config.mode === 'production') {
    var webpack = require('webpack');
    config.plugins.push(new webpack.optimize.LimitChunkCountPlugin({ maxChunks: 5 }));
}
