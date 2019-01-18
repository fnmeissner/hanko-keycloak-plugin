package io.hanko.plugin.keycloak.common;

import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.representations.AccessToken;

import javax.ws.rs.core.UriInfo;

public interface RequestAttributes {
    AccessToken getToken();
    HttpRequest getRequest();
    KeycloakContext getContext();
    UriInfo getUriInfo();
}
