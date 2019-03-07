package io.hanko.plugin.keycloak.profile;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.HankoClientConfig;
import io.hanko.client.java.models.ChangePassword;
import io.hanko.client.java.models.HankoDevice;
import io.hanko.client.java.models.HankoRegistrationRequest;
import io.hanko.client.java.models.HankoRequest;
import io.hanko.plugin.keycloak.authentication.HankoCredentialProvider;
import io.hanko.plugin.keycloak.authentication.HankoMultiAuthenticator;
import io.hanko.plugin.keycloak.common.HankoConfigurationException;
import io.hanko.plugin.keycloak.common.HankoResourceProvider;
import io.hanko.plugin.keycloak.common.HankoUtils;
import io.hanko.plugin.keycloak.serialization.*;
import org.keycloak.credential.CredentialModel;
import org.keycloak.forms.account.freemarker.model.RealmBean;
import org.keycloak.forms.account.freemarker.model.UrlBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.Cors;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class HankoProfileProvider extends HankoResourceProvider {
    private static ServicesLogger logger = ServicesLogger.LOGGER;

    private FreeMarkerUtil freeMarker;

    HankoProfileProvider(KeycloakSession session, HankoClient hankoClient, FreeMarkerUtil freeMarker) {
        super(session, hankoClient);
        this.freeMarker = freeMarker;
    }

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
            HankoClientConfig config = HankoUtils.createConfig(session);

            boolean hasRegisteredDevices = false;

            String hankoUserId = userStore.getHankoUserId(currentUser());
            if(hankoUserId != null) {
                hasRegisteredDevices = hankoClient.hasRegisteredDevices(config, hankoUserId);
            }

            HankoStatus status = new HankoStatus(isConfiguredForHanko && hasRegisteredDevices);
            Response.ResponseBuilder responseBuilder = Response.ok(status);
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);

        } catch(Exception ex) {
            String response = logAndFail("Could not fetch user devices from Hanko.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "GET", this);
        }
    }

    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response post() {
       return post("uaf");
    }

    @POST
    @Path("registerType/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@PathParam("type") String fidoTypeString) {
        ensureIsAuthenticatedUser();

        HankoClient.FidoType fidoType = HankoClient.FidoType.FIDO_UAF;
        try {
            fidoType = HankoClient.FidoType.valueOf(fidoTypeString);
        } catch (Exception ex) {
            logger.warn("no fido type declared, using UAF as fallback", ex);
        }

        String userIdHanko = userStore.getHankoUserId(currentUser());
        if (userIdHanko == null) {
            userIdHanko = UUID.randomUUID().toString();
            userStore.setHankoUserId(currentUser(), userIdHanko);
        }

        userStore.setHankoRequestId(currentUser(), null);

        String username = auth.getUser().getUsername();

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);

            String remoteAddress = context.getConnection().getRemoteAddr();
            HankoRegistrationRequest hankoRequest = hankoClient.requestRegistration(config, userIdHanko, username, remoteAddress, fidoType);

            userStore.setHankoRequestId(currentUser(), hankoRequest.id);

            String qrCode = hankoRequest.getQrCodeLink();
            HankoRegistrationChallenge hankoRegistrationChallenge = new HankoRegistrationChallenge(hankoRequest.id, qrCode, hankoRequest.request);

            Response.ResponseBuilder responseBuilder = Response.ok(hankoRegistrationChallenge);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Could not request Hanko registration.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }
    }

    @POST
    @Path("request/verify/webauthn")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerVerify(WebAuthnResponse webAuthnResponse) {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String requestId = userStore.getHankoRequestId(currentUser());

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "POST");
        }

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoRequest hankoRequest = hankoClient.validateWebAuthn(config, requestId, webAuthnResponse);
            Response.ResponseBuilder responseBuilder = Response.ok(hankoRequest);
            if(hankoRequest.isConfirmed()) {
                URI uri = context.getUri().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
                int maxCookieAge = 60 * 60 * 24 * 365; // 365 days
                responseBuilder.cookie(new NewCookie("LOGIN_METHOD", HankoMultiAuthenticator.LoginMethod.WEBAUTHN.name(), uri.getRawPath(), null, null, maxCookieAge, false, true));
            }
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Error while waiting for Hanko request to finish. ", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }
    }

    @POST
    @Path("register/complete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeRegistration() {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String requestId = userStore.getHankoRequestId(currentUser());

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "POST");
        }

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoRequest hankoRequest = hankoClient.awaitConfirmation(config, requestId);

            Response.ResponseBuilder responseBuilder = Response.ok(hankoRequest);

            if (hankoRequest.isConfirmed()) {
                UserCredentialModel credentials = new UserCredentialModel();
                credentials.setType(HankoCredentialProvider.TYPE);
                credentials.setValue(hankoUserId);
                session.userCredentialManager().updateCredential(context.getRealm(), auth.getUser(), credentials);
                URI uri = context.getUri().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
                int maxCookieAge = 60 * 60 * 24 * 365; // 365 days
                responseBuilder.cookie(new NewCookie("LOGIN_METHOD", HankoMultiAuthenticator.LoginMethod.UAF.name(), uri.getRawPath(), null, null, maxCookieAge, false, true));
            }

            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
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
            HankoClientConfig config = HankoUtils.createConfig(session);
            String remoteAddress = context.getConnection().getRemoteAddr();
            hankoClient.requestDeregistration(config, hankoUserId, username, remoteAddress);
        } catch (Exception ex) {
            String response = logAndFail("Could not deregister device at Hanko. " +
                    "The device will be disabled in Keycloak enyways.", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }

        session.userCredentialManager().disableCredentialType(context.getRealm(), auth.getUser(), HankoCredentialProvider.TYPE);

        HankoStatus hankoStatus = new HankoStatus(false);
        Response.ResponseBuilder responseBuilder = Response.ok(hankoStatus);
        return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
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

            HankoClientConfig config = HankoUtils.createConfig(session);


            attributes.put("realm", new RealmBean(context.getRealm()));
            attributes.put("keycloakUrl", baseUri);
            attributes.put("keycloakRealm", context.getRealm().getName());
            attributes.put("keycloakRealmId", context.getRealm().getId());
            attributes.put("keycloakClientId", "hanko-account");
            attributes.put("requires2fa", config.getRequries2fa().toString());

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
        } catch (HankoConfigurationException e) {
            logger.error("Failed to load hanko config",e);
            throw new RuntimeException(e);
        }

        return "";
    }

    @GET
    @Path("request/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response waitForRequest(@PathParam("requestId") String requestId) {
        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoRequest hankoRequest = hankoClient.awaitConfirmation(config, requestId);
            Response.ResponseBuilder responseBuilder = Response.ok(hankoRequest);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Error while waiting for Hanko request to finish. ", ex);
            Response.ResponseBuilder responseBuilder = Response.serverError().entity(response);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        }
    }

    @GET
    @Path("devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices() {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoDevice[] devices = new HankoDevice[]{};
            if(hankoUserId != null) {
                devices = hankoClient.getRegisteredDevices(config, hankoUserId);
            }

            Response.ResponseBuilder responseBuilder = Response.ok(devices);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Could not retrieve users devices", ex);
            return withError(response, Response.Status.INTERNAL_SERVER_ERROR, "GET");
        }
    }

    @DELETE
    @Path("devices/{fidoTypeString}/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDevice(@PathParam("fidoTypeString") String fidoTypeString, @PathParam("deviceId") String deviceId) {
        ensureIsAuthenticatedUser();

        String hankoUserId = userStore.getHankoUserId(currentUser());
        String username = auth.getUser().getUsername();

        if (hankoUserId == null) {
            return withError("HANKO User ID is null.", Response.Status.INTERNAL_SERVER_ERROR, "DELETE");
        }

        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoClient.FidoType fidoType = HankoClient.FidoType.valueOf(fidoTypeString);
            logger.error("deleting device");
            String remoteAddress = context.getConnection().getRemoteAddr();
            HankoRequest request = hankoClient.deleteDevice(config, hankoUserId, username, deviceId, remoteAddress, fidoType);
            logger.error("deleted device");

            Response.ResponseBuilder responseBuilder = Response.ok(request);
            return HankoUtils.withCorsNoCache(responseBuilder, "DELETE", this);
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
        return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
    }

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig() {
        try {
            HankoClientConfig config = HankoUtils.createConfig(session);
            HankoRealmConfig realmConfig = new HankoRealmConfig(config.getRequries2fa());
            Response.ResponseBuilder responseBuilder = Response.ok(realmConfig);
            return HankoUtils.withCorsNoCache(responseBuilder, "POST", this);
        } catch (Exception ex) {
            String response = logAndFail("Could not get config", ex);
            return withError(response, Response.Status.INTERNAL_SERVER_ERROR, "GET");
        }
    }

    private Response withError(String error, Response.Status status, String method) {
        ErrorMessage errorMessage = new ErrorMessage(error);
        Response.ResponseBuilder responseBuilder = Response.status(status).entity(errorMessage);
        return HankoUtils.withCorsNoCache(responseBuilder, method, this);
    }
}
