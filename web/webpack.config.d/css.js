config.resolve.modules.push("../../../../web/build/processedResources/Js/main");

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
                    path: '../../../../web/src/',
                },
            },
        }
    ],
});
