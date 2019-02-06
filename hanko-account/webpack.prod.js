const path = require('path')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const Dotenv = require('dotenv-webpack')
const HtmlPlugin = require('html-webpack-plugin')

module.exports = merge(common, {
  mode: 'production',
  optimization: {
    splitChunks: {
      chunks: 'all'
    }
  },
  output: {
    filename: 'index.[hash].js',
    chunkFilename: '[id].[hash].js',
    path: path.join(__dirname, 'dist'),
    publicPath: '/auth/resources/4.8.3.final/account/keycloak/js/'
  },
  plugins: [
    new Dotenv({ path: path.join(__dirname, '.env') }),
    new HtmlPlugin({
      minify: {
        collapseWhitespace: true
      },
      template: 'public/index.html',
      favicon: 'public/favicon.ico'
    })
  ]
})
