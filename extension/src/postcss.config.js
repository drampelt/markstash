module.exports = ({ options: { mode } }) => {
    process.env.NODE_ENV = mode; // Tailwind enables purging based on the NODE_ENV variable but setting it globally causes other problems
    return {
        plugins: [
            require('../../build/js/node_modules/postcss-import')({
                path: '../../node_modules'
            }),
            require('../../build/js/node_modules/postcss-nested'),
            require('../../build/js/node_modules/tailwindcss')('../../../../web/src/tailwind.config.js'),
            require('../../build/js/node_modules/autoprefixer'),
            mode === 'production' ? require('../../build/js/node_modules/cssnano')({preset: 'default'}) : false,
        ],
    };
};
