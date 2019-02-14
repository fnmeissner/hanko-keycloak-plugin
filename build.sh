#!/bin/bash

# exit on any error
set -e

echo Clean dist directory
rm -rf dist/
mkdir dist

echo Building ear package
mvn clean compile package

echo Buildung React frontend
cd hanko-account && npm install && npm run build-keycloak
cd ..

echo Copy themes to dist directory
cp -r themes dist/themes

echo Copy index.html
mkdir -p dist/themes/base/account
cp hanko-account/dist/index.html dist/themes/base/account/account-hanko.ftl

# echo Remove old javascript files
# rm themes/keycloak/account/resources/js/*
# rm themes/playground/account/resources/js/*.js
# rm themes/playground/account/resources/js/*.png

echo Copy new javascript files
cp hanko-account/dist/* dist/themes/keycloak/account/resources/js/
cp hanko-account/dist/*.js dist/themes/playground/account/resources/js/
# cp hanko-account/dist/*.png dist/themes/playground/account/resources/js/


echo Copy main.css
cp hanko-account/dist/main.css dist/themes/keycloak/account/resources/css/main.css
mkdir -p  dist/themes/playground/account/resources/css
cp hanko-account/dist/playground.css dist/themes/playground/account/resources/css/main.css


echo Zip new themes
cd dist
zip -r themes.zip themes
cd ..
