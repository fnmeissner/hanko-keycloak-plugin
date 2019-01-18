package io.hanko.plugin.keycloak.common;

import io.hanko.client.java.HankoClient;
import io.hanko.plugin.keycloak.authentication.HankoUserStore;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class HankoResourceProvider implements RealmResourceProvider, RequestAttributes {
    public final KeycloakSession session;
    public final KeycloakContext context;
    public final HankoClient hankoClient;
    public static ServicesLogger logger = ServicesLogger.LOGGER;

    public AuthenticationManager.AuthResult auth;
    public AccessToken token;
    public HankoUserStore userStore;

    public void ensureIsAuthenticatedUser() {
        this.auth = new AppAuthManager().authenticateBearerToken(session, session.getContext().getRealm());

        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }

        userStore = new HankoUserStore();
        token = auth.getToken();
    }

    public HankoResourceProvider(KeycloakSession session, HankoClient hankoClient) {
        this.session = session;
        this.context = session.getContext();
        this.hankoClient = hankoClient;
    }

    @Context
    public HttpRequest request;

    @Context
    public UriInfo uriInfo;

    @Override
    public AccessToken getToken() {
        return null;
    }

    @Override
    public HttpRequest getRequest() {
        return this.request;
    }

    @Override
    public KeycloakContext getContext() {
        return this.context;
    }

    @Override
    public UriInfo getUriInfo() {
        return this.uriInfo;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }

    public String logAndFail(String message, Exception ex) {
        logger.error(message, ex);
        return "An internal server error occurred, please check your logs.";
    }

    public UserModel currentUser() {
        return auth.getUser();
    }
}
