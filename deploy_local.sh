#!/usr/bin/env bash

mvn package
vagrant scp hanko-plugin-keycloak-ear/target/hanko-plugin-keycloak.ear :/opt/keycloak-4.5.0.Final/standalone/deployments/hanko-plugin-keycloak.ear
vagrant scp themes/base/login/login-hanko.ftl :/opt/keycloak-4.5.0.Final/themes/base/login/login-hanko.ftl
vagrant scp themes/keycloak/login/resources/img/login-hanko.png :/opt/keycloak-4.5.0.Final/themes/keycloak/login/resources/img/login-hanko.png
vagrant scp themes/base/account/account-hanko.ftl :/opt/keycloak-4.5.0.Final/themes/base/account/account-hanko.ftl
vagrant scp themes/keycloak/login/resources/js :/opt/keycloak-4.5.0.Final/themes/keycloak/login/resources
vagrant scp themes/keycloak/account/resources/js :/opt/keycloak-4.5.0.Final/themes/keycloak/account/resources
