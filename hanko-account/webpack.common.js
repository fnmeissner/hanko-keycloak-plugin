const path = require('path')
const HtmlPlugin = require('html-webpack-plugin')
const CleanWebpackPlugin = require('clean-webpack-plugin')

module.exports = {
  entry: ['@babel/polyfill', 'whatwg-fetch', './src/index.tsx'],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        use: [
          {
            loader: 'file-loader',
            options: {}
          }
        ]
      },
      {
        test: /\.mjs$/,
        include: /node_modules/,
        type: 'javascript/auto'
      },
      {
        test: /\.m?js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js', '.mjs']
  },
  output: {
    filename: 'index.[hash].js',
    chunkFilename: '[id].[hash].js',
    path: path.join(__dirname, 'dist'),
    publicPath: '/'
  },
  plugins: [new CleanWebpackPlugin(path.join(__dirname, 'dist'))]
}
