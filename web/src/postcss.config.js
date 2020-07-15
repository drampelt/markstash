module.exports = ({ env }) => ({
    plugins: [
        require('../../build/js/node_modules/postcss-import')({
            path: '../../node_modules'
        }),
        require('../../build/js/node_modules/postcss-nested'),
        require('../../build/js/node_modules/tailwindcss')('../../../../web/src/tailwind.config.js'),
        require('../../build/js/node_modules/autoprefixer'),
        env === 'production' ? require('../../build/js/node_modules/cssnano')({preset: 'default'}) : false,
    ],
});
