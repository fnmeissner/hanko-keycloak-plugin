const path = require('path')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const Dotenv = require('dotenv-webpack')
const HtmlPlugin = require('html-webpack-plugin')

module.exports = merge(common, {
  mode: 'development',
  devServer: {
    contentBase: path.join(__dirname, 'public'),
    historyApiFallback: true
  },
  devtool: 'inline-source-map',
  plugins: [
    new Dotenv({ path: path.join(__dirname, '.env.local') }),
    new HtmlPlugin({
      template: 'public/index.html',
      favicon: 'public/favicon.ico'
    })
  ]
})
