const path = require('path');
const webpack = require('webpack');

module.exports = {
  entry: './js-src/bootstrap.ts',
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'public/javascripts'),
    publicPath: '/assets/javascripts/'
  },
  devtool: 'source-map',
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
    extensions: ['.tsx', '.ts', '.js', 'wasm']
  },
  plugins: []
};
