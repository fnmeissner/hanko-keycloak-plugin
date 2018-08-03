#!/usr/bin/env bash

mvn package
vagrant scp hanko-plugin-keycloak-ear/target/hanko-plugin-keycloak.ear :/opt/keycloak-4.1.0.Final/standalone/deployments/hanko-plugin-keycloak.ear
vagrant scp templates/themes/base/login/login-hanko.ftl :/opt/keycloak-4.1.0.Final/themes/base/login/login-hanko.ftl
vagrant scp templates/themes/keycloak/login/resources/img/login-hanko.png :/opt/keycloak-4.1.0.Final/themes/keycloak/login/resources/img/login-hanko.png
vagrant scp templates/themes/base/account/account-hanko.ftl :/opt/keycloak-4.1.0.Final/themes/base/account/account-hanko.ftl
