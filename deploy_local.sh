#!/usr/bin/env bash

KCBASE=~/keycloak/keycloak-4.8.3.Final

mvn package
#cp hanko-plugin-keycloak-ear/target/hanko-plugin-keycloak.ear ${KCBASE}/standalone/deployments/hanko-plugin-keycloak.ear

${KCBASE}/bin/jboss-cli.sh --command="module remove --name=hanko-plugin-keycloak-ejb" || true

${KCBASE}/bin/jboss-cli.sh --command="module add --name=hanko-plugin-keycloak-ejb --resources=hanko-plugin-keycloak-ejb/target/hanko-plugin-keycloak-ejb-0.2-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-common,org.keycloak.keycloak-services,org.keycloak.keycloak-model-jpa,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.ws.rs.api,javax.persistence.api,org.hibernate,org.javassist,org.liquibase,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.core.jackson-annotations,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging,org.apache.httpcomponents,org.apache.commons.codec"

cp themes/base/login/login-hanko.ftl ${KCBASE}/themes/base/login/login-hanko.ftl
cp themes/base/login/hanko-username.ftl ${KCBASE}/themes/base/login/hanko-username.ftl
cp themes/base/login/hanko-multi-login.ftl ${KCBASE}/themes/base/login/hanko-multi-login.ftl
cp themes/base/login/hanko-multi-login-links.ftl ${KCBASE}/themes/base/login/hanko-multi-login-links.ftl
cp themes/keycloak/login/resources/img/login-hanko.png ${KCBASE}/themes/keycloak/login/resources/img/login-hanko.png
cp themes/base/account/account-hanko.ftl ${KCBASE}/themes/base/account/account-hanko.ftl
cp -r themes/keycloak/login/resources/js ${KCBASE}/themes/keycloak/login/resources
cp -r themes/keycloak/account/resources/js ${KCBASE}/themes/keycloak/account/resources
cp -r themes/hanko ${KCBASE}/themes
cp -r themes/playground ${KCBASE}/themes
