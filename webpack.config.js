'use strict';

// import Webpack plugins
const cleanPlugin = require('clean-webpack-plugin');
const ngAnnotatePlugin = require('ng-annotate-webpack-plugin');
const webpack = require('webpack');
const BowerWebpackPlugin = require("bower-webpack-plugin");
var path = require('path');

// define Webpack configuration object to be exported
let config = {
    context: path.join(__dirname, 'app'),
    entry: './app.module.js',
    output: {
        path: path.resolve(__dirname),
        filename: 'bundle.js'
    },
    resolve: {
        alias: {
            'npm': path.join(__dirname, 'node_modules'),
            'legacy': path.join(__dirname, 'lib'),
            "jquery-ui": path.join(__dirname, 'lib', 'jquery-ui.min.js')
        }
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                loader: 'style!css'
            },
            {
                test: /.js$/,
                loader: 'babel-loader',
                query: {
                    presets: ['es2015']
                }
            }
        ]
    },
    //devtool: 'source-map',
    devServer: {
        contentBase: `${__dirname}/`,
        port: 8082,
        proxy: {
            '/streampipes-backend': {
                target: 'http://localhost:8030',
                secure: false
            },
            '/visualizablepipeline': {
                target: 'http://localhost:5984',
                secure: false
            },
            '/dashboard': {
                target: 'http://localhost:5984',
                secure: false
            },
            '/pipeline': {
                target: 'http://localhost:5984',
                secure: false
            },
            '/streampipes/ws': {
                target: 'ws://ipe-koi04.fzi.de:61614',
                ws: true,
                secure: false
            }
        }
        //inline: true
    },
    plugins: [
        new cleanPlugin(['dist']),
        new ngAnnotatePlugin({
            add: true
        }),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery",
        }),
        new webpack.HotModuleReplacementPlugin(),
        //new webpack.OldWatchingPlugin()
        //new webpack.optimize.UglifyJsPlugin({
        //compress: {
        //warnings: false
        //}
        //})
    ]
};

module.exports = config;
