const path = require('path')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const Dotenv = require('dotenv-webpack')
const HtmlPlugin = require('html-webpack-plugin')

module.exports = merge(common, {
  mode: 'production',
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: 'ignore-loader'
      }
    ]
  },
  optimization: {
    splitChunks: {
      chunks: 'all'
    }
  },
  output: {
    filename: 'index.[hash].js',
    chunkFilename: '[id].[hash].js',
    path: path.join(__dirname, 'dist'),
    publicPath: '${url.resourcesPath}/js/'
  },
  plugins: [
    new Dotenv({ path: path.join(__dirname, '.env') }),
    new HtmlPlugin({
      minify: {
        collapseWhitespace: true
      },
      template: 'public/index.ftl',
      favicon: 'public/favicon.ico'
    })
  ]
})
