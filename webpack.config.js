const path = require('path');
const webpack = require('webpack');

module.exports = {
    entry: './js-src/index.tsx',
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'public/javascripts')
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            }
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js']
    },
    plugins: [
        new webpack.DefinePlugin({
            TDR_BASE_URL: JSON.stringify(process.env.TDR_BASE_URL || "http://localhost:9000"),
            TDR_AUTH_URL: JSON.stringify(process.env.TDR_AUTH_URL || "https://tdr.auth.eu-west-2.amazoncognito.com"),
            TDR_USER_POOL_ID: JSON.stringify(process.env.TDR_USER_POOL_ID || "eu-west-2_6Mn0M2i9C"),
            TDR_IDENTITY_POOL_ID: JSON.stringify(process.env.TDR_IDENTITY_POOL_ID || "eu-west-2:4b26364a-3070-4f98-8e86-1e33a1b54d85"),
            UPLOAD_APP_CLIENT_ID: JSON.stringify(process.env.UPLOAD_APP_CLIENT_ID || "2u2clbhcqnjaj3fn0jaid078ao")
        })
    ]
};