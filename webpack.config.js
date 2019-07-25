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
        extensions: [ '.tsx', '.ts', '.js' ]
    },
    plugins: [
        new webpack.DefinePlugin({
            TDR_BASE_URL: JSON.stringify(process.env.TDR_BASE_URL || "http://localhost:9000"),
            UPLOAD_APP_CLIENT_ID: JSON.stringify(process.env.UPLOAD_APP_CLIENT_ID || "2u2clbhcqnjaj3fn0jaid078ao")
        })
    ]
};