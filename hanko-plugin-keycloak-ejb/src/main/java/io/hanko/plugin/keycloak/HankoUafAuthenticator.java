package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.models.HankoRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static io.hanko.plugin.keycloak.HankoUtils.getApiKey;
import static io.hanko.plugin.keycloak.HankoUtils.getApiKeyId;
import static io.hanko.plugin.keycloak.HankoUtils.getApiUrl;

public class HankoUafAuthenticator extends AbstractUsernameFormAuthenticator implements Authenticator {
    private static ServicesLogger logger = ServicesLogger.LOGGER;

    private final HankoUserStore userStore;
    private final HankoClient hankoClient;

    HankoUafAuthenticator(HankoUserStore userStore, HankoClient hankoClient) {
        this.userStore = userStore;
        this.hankoClient = hankoClient;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            logger.debug("login canceled by user");
            cancelLogin(context);
            return;
        }

        logger.debug("waiting for challenge response");

        // get current Hanko request ID
        UserModel currentUser = context.getUser();
        String hankoRequestId = userStore.getHankoRequestId(currentUser);

        try {
            // get Hanko API key from the Hanko UAF Authenticator configuration
            String apiUrl = getApiUrl(context.getAuthenticatorConfig());
            String apikey = getApiKey(context.getAuthenticatorConfig());
            String apikeyId = getApiKeyId(context.getAuthenticatorConfig());

            // blocking call to Hanko API
            HankoRequest hankoRequest = hankoClient.awaitConfirmation(hankoRequestId, apiUrl, apikey, apikeyId);

            if (hankoRequest.isConfirmed()) {
                context.success();
            } else {
                logger.warn("Authentication failed for user " + context.getUser().getUsername());
                cancelLogin(context);
            }
        } catch (Exception ex) {
            logger.error("Hanko request verification failed.", ex);
            cancelLogin(context);
        }
    }

    private void cancelLogin(AuthenticationFlowContext context) {
        context.cancelLogin();
        context.clearUser();
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        AuthenticationSessionModel authSession = authenticationFlowContext.getAuthenticationSession();

        if (HankoUtils.isUserAuthenticated(authSession)) {
            authenticationFlowContext.success();
            HankoUtils.removeHankoRequiredAuthNote(authSession);
            return;
        }

        UserModel currentUser = authenticationFlowContext.getUser();
        String userId = userStore.getHankoUserId(currentUser);
        String username = currentUser.getUsername();

        try {
            String apiUrl = getApiUrl(authenticationFlowContext.getAuthenticatorConfig());
            String apikey = getApiKey(authenticationFlowContext.getAuthenticatorConfig());
            String apikeyId = getApiKeyId(authenticationFlowContext.getAuthenticatorConfig());
            String remoteAddress = authenticationFlowContext.getConnection().getRemoteAddr();
            HankoRequest hankoRequest = hankoClient.requestAuthentication(userId, username, apiUrl, apikey, apikeyId, remoteAddress);
            userStore.setHankoRequestId(currentUser, hankoRequest.id);

            Response response = authenticationFlowContext.form().setAttribute("requestId", hankoRequest.id).createForm("login-hanko.ftl");
            authenticationFlowContext.challenge(response);

        } catch (HankoConfigurationException ex) {
            logger.error("Could not create Hanko request.", ex);
            cancelLogin(authenticationFlowContext);
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return keycloakSession.userCredentialManager().isConfiguredFor(realmModel, userModel, HankoCredentialProvider.TYPE);
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        // none
    }
}
