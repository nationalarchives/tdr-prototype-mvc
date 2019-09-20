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
    ]
};