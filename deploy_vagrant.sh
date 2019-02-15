#!/usr/bin/env bash

set -e

./build.sh

vagrant scp dist/hanko-plugin-keycloak-ejb-0.2-SNAPSHOT.jar :/opt/keycloak-4.8.3.Final/hanko-plugin-keycloak.jar
vagrant scp dist/themes.zip :/opt/keycloak-4.8.3.Final/themes.zip
vagrant ssh -c 'cd /opt/keycloak-4.8.3.Final; ./bin/jboss-cli.sh --command="module remove --name=hanko-plugin-keycloak-ejb" || true; ./bin/jboss-cli.sh --command="module add --name=hanko-plugin-keycloak-ejb --resources=./hanko-plugin-keycloak.jar --dependencies=org.keycloak.keycloak-common,org.keycloak.keycloak-core,org.keycloak.keycloak-services,org.keycloak.keycloak-model-jpa,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.ws.rs.api,javax.persistence.api,org.hibernate,org.javassist,org.liquibase,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.core.jackson-annotations,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging,org.apache.httpcomponents,org.apache.commons.codec"; unzip -o themes.zip -d .'
