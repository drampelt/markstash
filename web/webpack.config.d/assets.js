const SpriteLoaderPlugin = require('svg-sprite-loader/plugin');

config.module.rules.push({
    test: /\.svg$/,
    use: [
        {
            loader: 'svg-sprite-loader',
            options: {
                extract: true,
                publicPath: '/static/'
            }
        },
    ]
});

config.module.rules.push({
    test: /\.(png|jpe?g|gif)(\?.*)?$/,
    use: {
        loader: 'url-loader',
        query: {
            limit: 10000,
            name: 'imgs/[name]--[folder].[ext]'
        }
    }
});

config.module.rules.push({
    test: /\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/,
    loader: 'url-loader',
    options: {
        limit: 10000,
        name: 'media/[name]--[folder].[ext]'
    }
});

config.module.rules.push({
    test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
    use: {
        loader: 'url-loader',
        query: {
            limit: 10000,
            name: 'fonts/[name]--[folder].[ext]'
        }
    }
});

config.output.publicPath = '/';

config.plugins.push(new SpriteLoaderPlugin());

