package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.models.ChangePassword;
import io.hanko.client.java.models.HankoDevice;
import io.hanko.client.java.models.HankoRegistrationRequest;
import io.hanko.client.java.models.HankoRequest;
import io.hanko.plugin.keycloak.serialization.ErrorMessage;
import io.hanko.plugin.keycloak.serialization.HankoRegistrationChallenge;
import io.hanko.plugin.keycloak.serialization.HankoStatus;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.credential.CredentialModel;
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
                .allowedMethods("GET", "POST", "DELETE")
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

        try {
            String hankoUserId = userStore.getHankoUserId(currentUser());
            boolean hasRegisteredDevices = hankoClient.hasRegisteredDevices(hankoUserId, HankoUtils.getApiUrl(session),
                    HankoUtils.getApiKey(session), HankoUtils.getApiKeyId(session));

            HankoStatus status = new HankoStatus(isConfiguredForHanko && hasRegisteredDevices);
            Response.ResponseBuilder responseBuilder = Response.ok(status);
            return withCorsNoCache(responseBuilder, "GET");

        } catch(Exception ex) {
            String response = logAndFail("Could not fetch user devices from Hanko.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return withCorsNoCache(responseBuilder, "GET");
        }
    }

    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response post() {
        ensureIsAuthenticatedUser();

        String userIdHanko = userStore.getHankoUserId(currentUser());
        if (userIdHanko == null) {
            userIdHanko = UUID.randomUUID().toString();
            userStore.setHankoUserId(currentUser(), userIdHanko);
        }

        userStore.setHankoRequestId(currentUser(), null);

        String username = auth.getUser().getUsername();

        try {
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apiKeyId = HankoUtils.getApiKeyId(session);

            String remoteAddress = context.getConnection().getRemoteAddr();
            HankoRegistrationRequest hankoRequest = hankoClient.requestRegistration(userIdHanko, username, apiUrl, apikey, apiKeyId, remoteAddress);

            userStore.setHankoRequestId(currentUser(), hankoRequest.id);

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
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apikeyId = HankoUtils.getApiKeyId(session);
            HankoRequest hankoRequest = hankoClient.awaitConfirmation(requestId, apiUrl, apikey, apikeyId);

            if (hankoRequest.isConfirmed()) {
                UserCredentialModel credentials = new UserCredentialModel();
                credentials.setType(HankoCredentialProvider.TYPE);
                credentials.setValue(hankoUserId);
                session.userCredentialManager().updateCredential(context.getRealm(), auth.getUser(), credentials);
            }

            Response.ResponseBuilder responseBuilder = Response.ok(hankoRequest);
            return withCorsNoCache(responseBuilder, "POST");
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

        userStore.setHankoRequestId(currentUser(), null);

        try {
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apiKeyId = HankoUtils.getApiKeyId(session);
            hankoClient.requestDeregistration(hankoUserId, username, apiUrl, apikey, apiKeyId);
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

            String redirectUrl = uriInfo.getQueryParameters().getFirst("redirect_url");
            if (redirectUrl != null && !redirectUrl.equals("")) {
                attributes.put("redirect_url", redirectUrl);
            }

            String redirectName = uriInfo.getQueryParameters().getFirst("redirect_name");
            if (redirectName != null && !redirectName.equals("")) {
                attributes.put("redirect_name", redirectName);
            }

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
            attributes.put("keycloakRealmId", context.getRealm().getId());
            attributes.put("keycloakClientId", "hanko-account");
            attributes.put("url", new UrlBean(session.getContext().getRealm(), theme, baseUri, baseQueryUri, uriInfo.getRequestUri(), stateChecker));

            if (auth != null && auth.getUser() != null) {
                Locale locale = session.getContext().resolveLocale(auth.getUser());
                attributes.put("locale", locale);
            }

            return freeMarker.processTemplate(attributes, "account-hanko.ftl", theme);
        } catch (FreeMarkerException e) {
            logger.error("Failed to process template", e);
        } catch (IOException e) {
            logger.error("Failed to load theme", e);
        }

        return "";
    }

    @GET
    @Path("request/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response waitForRequest(@PathParam("requestId") String requestId) {
        try {
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apikeyId = HankoUtils.getApiKeyId(session);

            HankoRequest hankoRequest = hankoClient.awaitConfirmation(requestId, apiUrl, apikey, apikeyId);
            Response.ResponseBuilder responseBuilder = Response.ok(hankoRequest);

            return withCorsNoCache(responseBuilder, "POST");

        } catch (Exception ex) {
            String response = logAndFail("Error while waiting for Hanko request to finish. ", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return withCorsNoCache(responseBuilder, "POST");
        }
    }

    @GET
    @Path("devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices() {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String requestId = userStore.getHankoRequestId(currentUser());

        AccessToken token = auth.getToken();

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "POST");
        }

        try {
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apikeyId = HankoUtils.getApiKeyId(session);
            HankoDevice[] devices = hankoClient.getRegisteredDevices(hankoUserId, apiUrl, apikey, apikeyId);

            Response.ResponseBuilder responseBuilder = Response.ok(devices);
            return withCorsNoCache(responseBuilder, "POST");
        } catch (Exception ex) {
            String response = logAndFail("Could not retrieve users devices", ex);
            return withError(response, Response.Status.INTERNAL_SERVER_ERROR, "GET");
        }
    }

    @DELETE
    @Path("devices/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDevice(@PathParam("deviceId") String deviceId) {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String username = auth.getUser().getUsername();

        AccessToken token = auth.getToken();

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "DELETE");
        }

        try {
            String apiUrl = HankoUtils.getApiUrl(session);
            String apikey = HankoUtils.getApiKey(session);
            String apikeyId = HankoUtils.getApiKeyId(session);
            HankoRequest request = hankoClient.deleteDevice(hankoUserId, username, deviceId, apiUrl, apikey, apikeyId);

            Response.ResponseBuilder responseBuilder = Response.ok(request);
            return withCorsNoCache(responseBuilder, "DELETE");
        } catch (Exception ex) {
            String response = logAndFail("Could not delete users device", ex);
            return withError(response, Response.Status.INTERNAL_SERVER_ERROR, "DELETE");
        }
    }

    @POST
    @Path("password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangePassword changePassword) {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String username = currentUser().getUsername();

        UserCredentialModel credentials = new UserCredentialModel();
        credentials.setType(CredentialModel.PASSWORD);
        credentials.setValue(changePassword.newPassword);

        session.userCredentialManager().updateCredential(context.getRealm(), auth.getUser(), credentials);


        Response.ResponseBuilder responseBuilder = Response.ok();
        return withCorsNoCache(responseBuilder, "POST");
    }

    private Response withError(String error, Response.Status status, String method) {
        ErrorMessage errorMessage = new ErrorMessage(error);
        Response.ResponseBuilder responseBuilder = Response.status(status).entity(errorMessage);
        return withCorsNoCache(responseBuilder, method);
    }

    private Response withCorsNoCache(Response.ResponseBuilder responseBuilder, String method) {
        Response.ResponseBuilder withoutCache = responseBuilder.cacheControl(CacheControlUtil.noCache());
        if(token != null) {
            return Cors.add(request, withoutCache)
                    .allowedMethods(method)
                    .allowedOrigins(token)
                    .auth()
                    .build();
        } else if(context.getClient() != null) {
            return Cors.add(request, withoutCache)
                    .allowedMethods(method)
                    .allowedOrigins(uriInfo, context.getClient())
                    .auth()
                    .build();
        } else {
            return Cors.add(request, withoutCache)
                    .allowAllOrigins()
                    .allowedMethods(method)
                    .auth()
                    .build();
        }
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
