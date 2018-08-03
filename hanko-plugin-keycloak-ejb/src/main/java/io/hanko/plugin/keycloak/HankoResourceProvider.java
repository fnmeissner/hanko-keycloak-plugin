package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.models.HankoRegistrationRequest;
import io.hanko.client.java.models.HankoRequest;
import io.hanko.plugin.keycloak.serialization.ErrorMessage;
import io.hanko.plugin.keycloak.serialization.HankoRegistrationChallenge;
import io.hanko.plugin.keycloak.serialization.HankoStatus;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.forms.account.freemarker.model.RealmBean;
import org.keycloak.forms.account.freemarker.model.UrlBean;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.Cors;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class HankoResourceProvider implements RealmResourceProvider {
    private final KeycloakSession session;
    private final KeycloakContext context;
    private final HankoClient hankoClient;

    private static ServicesLogger logger = ServicesLogger.LOGGER;

    private AuthenticationManager.AuthResult auth;
    private AccessToken token;
    private HankoUserStore userStore;
    private FreeMarkerUtil freeMarker;

    HankoResourceProvider(KeycloakSession session, HankoClient hankoClient, FreeMarkerUtil freeMarker) {
        this.session = session;
        this.context = session.getContext();
        this.hankoClient = hankoClient;
        this.freeMarker = freeMarker;
    }

    @Context
    protected HttpRequest request;

    @Context
    private UriInfo uriInfo;

    @Override
    public Object getResource() {
        return this;
    }

    @OPTIONS
    @Path("{any:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVersionPreflight() {
        if (request == null) {
            logger.error("Request null");
        }
        return Cors.add(request, Response.ok())
                .allowedMethods("GET", "POST")
                .allowedOrigins(uriInfo, context.getClient())
                .preflight()
                .auth()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        ensureIsAuthenticatedUser();

        boolean isConfiguredForHanko = session.userCredentialManager().isConfiguredFor(
                context.getRealm(), auth.getUser(), HankoCredentialProvider.TYPE);

        HankoStatus status = new HankoStatus(isConfiguredForHanko);
        Response.ResponseBuilder responseBuilder = Response.ok(status);
        return withCorsNoCache(responseBuilder, "GET");
    }

    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response post() {
        ensureIsAuthenticatedUser();

        userStore.setHankoUserId(currentUser(), null);
        userStore.setHankoRequestId(currentUser(), null);

        String userIdHanko = UUID.randomUUID().toString();
        String username = auth.getUser().getUsername();

        try {
            String apikey = HankoUtils.getApiKey(session);

            String remoteAddress = context.getConnection().getRemoteAddr();
            HankoRegistrationRequest hankoRequest = hankoClient.requestRegistration(userIdHanko, username, apikey, remoteAddress);

            userStore.setHankoRequestId(currentUser(), hankoRequest.id);
            userStore.setHankoUserId(currentUser(), userIdHanko);

            String qrCode = hankoRequest.getQrCodeLink();
            HankoRegistrationChallenge hankoRegistrationChallenge = new HankoRegistrationChallenge(qrCode);

            Response.ResponseBuilder responseBuilder = Response.ok(hankoRegistrationChallenge);
            return withCorsNoCache(responseBuilder, "POST");
        } catch (Exception ex) {
            String response = logAndFail("Could not request Hanko registration.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return withCorsNoCache(responseBuilder, "POST");
        }
    }

    @POST
    @Path("register/complete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeRegistration() {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String requestId = userStore.getHankoRequestId(currentUser());

        AccessToken token = auth.getToken();

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "POST");
        }

        try {
            String apikey = HankoUtils.getApiKey(session);
            HankoRequest hankoRequest = hankoClient.awaitConfirmation(requestId, apikey);

            if (hankoRequest.isConfirmed()) {
                UserCredentialModel credentials = new UserCredentialModel();
                credentials.setType(HankoCredentialProvider.TYPE);
                credentials.setValue(hankoUserId);
                session.userCredentialManager().updateCredential(context.getRealm(), auth.getUser(), credentials);

                HankoStatus hankoStatus = new HankoStatus(false);
                Response.ResponseBuilder responseBuilder = Response.ok(hankoStatus);
                return withCorsNoCache(responseBuilder, "POST");
            }

            return withError("HANKO request confirmation failed", Response.Status.FORBIDDEN, "POST");
        } catch (Exception ex) {
            String response = logAndFail("Could not request Hanko registration", ex);
            return withError(response, Response.Status.INTERNAL_SERVER_ERROR, "POST");
        }
    }

    @POST
    @Path("deregister")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete() {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String username = currentUser().getUsername();

        userStore.setHankoUserId(currentUser(), null);
        userStore.setHankoRequestId(currentUser(), null);

        try {
            String apikey = HankoUtils.getApiKey(session);
            hankoClient.requestDeregistration(hankoUserId, username, apikey);
        } catch (Exception ex) {
            String response = logAndFail("Could not deregister device at Hanko. " +
                    "The device will be disabled in Keycloak enyways.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return withCorsNoCache(responseBuilder, "POST");
        }

        session.userCredentialManager().disableCredentialType(context.getRealm(), auth.getUser(), HankoCredentialProvider.TYPE);

        HankoStatus hankoStatus = new HankoStatus(false);
        Response.ResponseBuilder responseBuilder = Response.ok(hankoStatus);
        return withCorsNoCache(responseBuilder, "POST");
    }

    @GET
    @Path("status")
    @Produces(MediaType.TEXT_HTML)
    public String account() {
        Map<String, Object> attributes = new HashMap<>();

        try {
            Theme theme = session.theme().getTheme(Theme.Type.ACCOUNT);
            UriInfo uriInfo = session.getContext().getUri();
            URI baseUri = uriInfo.getBaseUri();
            UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
            for (Map.Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
                baseUriBuilder.queryParam(e.getKey(), e.getValue().toArray());
            }
            URI baseQueryUri = baseUriBuilder.build();
            String stateChecker = (String) session.getAttribute("state_checker");
            if (stateChecker != null) {
                attributes.put("stateChecker", stateChecker);
            }

            try {
                attributes.put("properties", theme.getProperties());
            } catch (IOException e) {
                logger.warn("Failed to load properties", e);
            }

            attributes.put("realm", new RealmBean(context.getRealm()));
            attributes.put("keycloakUrl", baseUri);
            attributes.put("keycloakRealm", context.getRealm().getName());
            attributes.put("keycloakClientId", "hanko-account");
            attributes.put("url", new UrlBean(session.getContext().getRealm(), theme, baseUri, baseQueryUri, uriInfo.getRequestUri(), stateChecker));

            return freeMarker.processTemplate(attributes, "account-hanko.ftl", theme);
        } catch (FreeMarkerException e) {
            logger.error("Failed to process template", e);
        } catch (IOException e) {
            logger.error("Failed to load theme", e);
        }

        return "";
    }

    private Response withError(String error, Response.Status status, String method) {
        ErrorMessage errorMessage = new ErrorMessage(error);
        Response.ResponseBuilder responseBuilder = Response.status(status).entity(errorMessage);
        return withCorsNoCache(responseBuilder, method);
    }

    private Response withCorsNoCache(Response.ResponseBuilder responseBuilder, String method) {
        Response.ResponseBuilder withoutCache = responseBuilder.cacheControl(CacheControlUtil.noCache());
        return Cors.add(request, withoutCache)
                .allowedMethods(method)
                .allowedOrigins(token)
                .auth()
                .build();
    }

    private String logAndFail(String message, Exception ex) {
        logger.error(message, ex);
        return "An internal server error occurred, please check your logs.";
    }

    private void ensureIsAuthenticatedUser() {
        this.auth = new AppAuthManager().authenticateBearerToken(session, session.getContext().getRealm());

        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        }

        userStore = new HankoUserStore();
        token = auth.getToken();
    }

    private UserModel currentUser() {
        return auth.getUser();
    }

    @Override
    public void close() {
    }

}
