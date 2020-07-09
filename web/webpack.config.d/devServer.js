if (config.mode !== 'production') {
    config.devServer = Object.assign(
        {},
        config.devServer || {},
        {
            port: 8081,
            open: false,
            historyApiFallback: true,
            proxy: {
                '/api': 'http://localhost:8080',
            },
        },
    );
}
