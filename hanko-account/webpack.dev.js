const path = require('path')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const Dotenv = require('dotenv-webpack')
const HtmlPlugin = require('html-webpack-plugin')

module.exports = merge(common, {
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: [
          'style-loader', // creates style nodes from JS strings
          'css-loader', // translates CSS into CommonJS
          'sass-loader' // compiles Sass to CSS, using Node Sass by default
        ]
      }
    ]
  },
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
