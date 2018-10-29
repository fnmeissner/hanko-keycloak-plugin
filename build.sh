#!/bin/bash

echo Building ear package
mvn clean compile package

echo Buildung React frontend
cd hanko-account && npm install && npm run build-keycloak
cd ..

echo Copy index.html
cp hanko-account/dist/index.html themes/base/account/account-hanko.ftl

echo Remove old javascript files
rm themes/keycloak/account/resources/js/*

echo Copy new javascript files
cp hanko-account/dist/*.js themes/keycloak/account/resources/js/

echo Delete old themes.zip
rm themes.zip

echo Zip new themes
zip -r themes.zip themes
