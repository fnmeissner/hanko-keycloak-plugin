#!/usr/bin/env bash

mvn package
cp hanko-plugin-keycloak-ear/target/hanko-plugin-keycloak.ear ~/keycloak/keycloak-4.5.0.Final/standalone/deployments/hanko-plugin-keycloak.ear
cp themes/base/login/login-hanko.ftl ~/keycloak/keycloak-4.5.0.Final/themes/base/login/login-hanko.ftl
cp themes/base/login/hanko-multi-login.ftl ~/keycloak/keycloak-4.5.0.Final/themes/base/login/hanko-multi-login.ftl
cp themes/base/login/hanko-multi-login-links.ftl ~/keycloak/keycloak-4.5.0.Final/themes/base/login/hanko-multi-login-links.ftl
cp themes/keycloak/login/resources/img/login-hanko.png ~/keycloak/keycloak-4.5.0.Final/themes/keycloak/login/resources/img/login-hanko.png
cp themes/base/account/account-hanko.ftl ~/keycloak/keycloak-4.5.0.Final/themes/base/account/account-hanko.ftl
cp -r themes/keycloak/login/resources/js ~/keycloak/keycloak-4.5.0.Final/themes/keycloak/login/resources
cp -r themes/keycloak/account/resources/js ~/keycloak/keycloak-4.5.0.Final/themes/keycloak/account/resources
cp -r themes/hanko ~/keycloak/keycloak-4.5.0.Final/themes

