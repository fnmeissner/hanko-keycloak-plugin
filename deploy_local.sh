#!/usr/bin/env bash

KCBASE=~/keycloak/keycloak-4.8.3.Final

./build.sh

${KCBASE}/bin/jboss-cli.sh --command="module remove --name=hanko-plugin-keycloak-ejb" || true

${KCBASE}/bin/jboss-cli.sh --command="module add --name=hanko-plugin-keycloak-ejb --resources=hanko-plugin-keycloak-ejb/target/hanko-plugin-keycloak-ejb-0.2-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-common,org.keycloak.keycloak-services,org.keycloak.keycloak-model-jpa,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.ws.rs.api,javax.persistence.api,org.hibernate,org.javassist,org.liquibase,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.core.jackson-annotations,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging,org.apache.httpcomponents,org.apache.commons.codec"

unzip -o dist/themes.zip -d ${KCBASE}
