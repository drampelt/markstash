config.resolve.modules.push("../../../../extension/build/processedResources/Js/main");

config.module.rules.push({
    test: /\.css$/,
    use: [
        {
            loader: 'style-loader',
        },
        {
            loader: 'css-loader',
            options: {
                importLoaders: 1,
            },
        },
        {
            loader: 'postcss-loader',
            options: {
                config: {
                    path: '../../../../extension/src/',
                    ctx: {
                        mode: config.mode,
                    },
                },
            },
        }
    ],
});
