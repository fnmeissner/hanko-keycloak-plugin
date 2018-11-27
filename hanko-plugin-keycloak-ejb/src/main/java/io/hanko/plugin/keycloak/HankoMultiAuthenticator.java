package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.HankoClientConfig;
import io.hanko.client.java.models.HankoDevice;
import io.hanko.client.java.models.HankoRequest;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ServicesLogger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class HankoMultiAuthenticator extends AbstractUsernameFormAuthenticator implements Authenticator {
    private static ServicesLogger logger = ServicesLogger.LOGGER;

    private final HankoUserStore userStore;
    private final HankoClient hankoClient;

    private enum LoginMethod {PASSWORD, UAF, WEBAUTHN} //ROAMING_AUTHENTICATOR, PLATFORM_AUTHENTICATOR}

    HankoMultiAuthenticator(HankoUserStore userStore, HankoClient hankoClient) {
        this.userStore = userStore;
        this.hankoClient = hankoClient;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("cancel")) {
            context.resetFlow();
            return;
        }

        if (formData.containsKey("switch")) {
            LoginMethod loginMethod = LoginMethod.valueOf(formData.getFirst("loginMethod"));
            renderChallenge(loginMethod, context, getDevices(context));
            return;
        }

        // get current Hanko request ID
        UserModel currentUser = context.getUser();
        if(currentUser == null) {
            logger.error("user is null");
        } else {
            logger.error("current user: " + currentUser.getUsername());
        }

        String hankoRequestId = userStore.getHankoRequestId(currentUser);

        LoginMethod loginMethod = LoginMethod.valueOf(formData.getFirst("loginMethod"));

        switch (loginMethod) {
            case UAF:
                logger.error("checking uaf");
                try {
                    // get Hanko API key from the Hanko UAF Authenticator configuration
                    HankoClientConfig config = HankoUtils.createConfig(context.getSession());

                    // blocking call to Hanko API
                    HankoRequest hankoRequest = hankoClient.awaitConfirmation(config, hankoRequestId);

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
                break;

            case WEBAUTHN:
                logger.error("checking webauthn authenticator");
                try {
                    // get Hanko API key from the Hanko UAF Authenticator configuration
                    HankoClientConfig config = HankoUtils.createConfig(context.getSession());

                    // blocking call to Hanko API
                    String hankoResponse = formData.getFirst("hankoresponse");
                    HankoRequest hankoRequest = hankoClient.validateWebAuthn(config, hankoRequestId, hankoResponse);

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
                break;

            default:
                if (!validatePassword(context, currentUser, formData)) {
                    context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
                    cancelLogin(context);
                    return;
                }

                if (!enabledUser(context, currentUser)) {
                    cancelLogin(context);
                    return;
                }

                logger.error("login successful");

                context.success();
                break;
        }
    }

    @Override
    public boolean validatePassword(AuthenticationFlowContext context, UserModel user, MultivaluedMap<String, String> inputData) {
        List<CredentialInput> credentials = new LinkedList<>();
        String password = inputData.getFirst(CredentialRepresentation.PASSWORD);
        credentials.add(UserCredentialModel.password(password));

        if (isTemporarilyDisabledByBruteForce(context, user)) return false;

        if (password != null && !password.isEmpty() && context.getSession().userCredentialManager().isValid(context.getRealm(), user, credentials)) {
            return true;
        } else {
            context.getEvent().user(user);
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            context.clearUser();
            return false;
        }
    }

    @Override
    public boolean enabledUser(AuthenticationFlowContext context, UserModel user) {
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            Response challengeResponse = disabledUser(context);
            // this is not a failure so don't call failureChallenge.
            //context.failureChallenge(AuthenticationFlowError.USER_DISABLED, challengeResponse);
            context.forceChallenge(challengeResponse);
            return false;
        }
        if (isTemporarilyDisabledByBruteForce(context, user)) return false;
        return true;
    }

    private boolean isTemporarilyDisabledByBruteForce(AuthenticationFlowContext context, UserModel user) {
        if (context.getRealm().isBruteForceProtected()) {
            if (context.getProtector().isTemporarilyDisabled(context.getSession(), context.getRealm(), user)) {
                context.getEvent().user(user);
                context.getEvent().error(Errors.USER_TEMPORARILY_DISABLED);
                Response challengeResponse = temporarilyDisabledUser(context);
                // this is not a failure so don't call failureChallenge.
                //context.failureChallenge(AuthenticationFlowError.USER_TEMPORARILY_DISABLED, challengeResponse);
                context.forceChallenge(challengeResponse);
                return true;
            }
        }
        return false;
    }

    private void cancelLogin(AuthenticationFlowContext context) {
        context.clearUser();
        context.resetFlow();
    }

    private List<HankoDevice> getDevices(AuthenticationFlowContext authenticationFlowContext) {
        List<HankoDevice> devices = new LinkedList<>();

        UserModel currentUser = authenticationFlowContext.getUser();
        String userId = userStore.getHankoUserId(currentUser);

        if (!(userId == null || userId.trim().isEmpty())) {
            try {
                HankoClientConfig config = HankoUtils.createConfig(authenticationFlowContext.getSession());
                devices = Arrays.asList(hankoClient.getRegisteredDevices(config, userId));
            } catch (Exception ex) {
                logger.error("Could not fetch user devices", ex);
            }
        }

        return devices;
    }

    private boolean hasUaf(List<HankoDevice> devices) {
        return devices.stream().anyMatch(device -> Objects.equals(device.authenticatorType, "FIDO_UAF"));
    }

    private boolean hasWebAuthn(List<HankoDevice> devices) {
        return devices.stream().anyMatch(device -> Objects.equals(device.authenticatorType, "WEBAUTHN"));
    }

    private LoginMethod getLoginMethod(AuthenticationFlowContext authenticationFlowContext, List<HankoDevice> devices) {
        UserModel currentUser = authenticationFlowContext.getUser();
        String userId = userStore.getHankoUserId(currentUser);

        if (userId == null || userId.trim().isEmpty()) {
            return LoginMethod.PASSWORD;
        } else {

            boolean hasWebAuthnAuthenticator = hasWebAuthn(devices);
            boolean hasUafAuthenticator = hasUaf(devices);

            if (hasWebAuthnAuthenticator) {
                return LoginMethod.WEBAUTHN;
            } else if (hasUafAuthenticator) {
                return LoginMethod.UAF;
            } else {
                return LoginMethod.PASSWORD;
            }
        }
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        List<HankoDevice> devices = getDevices(authenticationFlowContext);
        LoginMethod loginMethod = getLoginMethod(authenticationFlowContext, devices);
        renderChallenge(loginMethod, authenticationFlowContext, devices);
    }

    private void renderChallenge(LoginMethod loginMethod, AuthenticationFlowContext authenticationFlowContext, List<HankoDevice> devices) {
        UserModel currentUser = authenticationFlowContext.getUser();
        String userId = userStore.getHankoUserId(currentUser);
        String username = currentUser.getUsername();

        LoginFormsProvider formsProvider = authenticationFlowContext.form();
        formsProvider.setAttribute("hasUaf", hasUaf(devices));
        formsProvider.setAttribute("hasWebAuthn", hasWebAuthn(devices));

        switch (loginMethod) {
            case UAF:
                logger.error("using uaf");
                try {
                    HankoClientConfig config = HankoUtils.createConfig(authenticationFlowContext.getSession());
                    String remoteAddress = authenticationFlowContext.getConnection().getRemoteAddr();
                    HankoRequest hankoRequest = hankoClient.requestAuthentication(config, userId, username, remoteAddress, HankoClient.FidoType.FIDO_UAF);
                    userStore.setHankoRequestId(currentUser, hankoRequest.id);

                    Response response = formsProvider.setAttribute("requestId", hankoRequest.id).setAttribute("loginMethod", "UAF").createForm("hanko-multi-login.ftl");
                    authenticationFlowContext.challenge(response);

                } catch (HankoConfigurationException ex) {
                    logger.error("Could not create Hanko request.", ex);
                    cancelLogin(authenticationFlowContext);
                }
                break;
            case WEBAUTHN:
                logger.error("using webauthn authenticator");
                try {
                    HankoClientConfig config = HankoUtils.createConfig(authenticationFlowContext.getSession());
                    String remoteAddress = authenticationFlowContext.getConnection().getRemoteAddr();
                    HankoRequest hankoRequest = hankoClient.requestAuthentication(config, userId, username, remoteAddress, HankoClient.FidoType.WEBAUTHN);
                    userStore.setHankoRequestId(currentUser, hankoRequest.id);

                    Response response = formsProvider.setAttribute("request", hankoRequest.request).setAttribute("loginMethod", "WEBAUTHN").createForm("hanko-multi-login.ftl");
                    authenticationFlowContext.challenge(response);

                } catch (HankoConfigurationException ex) {
                    logger.error("Could not create Hanko request.", ex);
                    cancelLogin(authenticationFlowContext);
                }
                break;
            default:
                Response response = formsProvider.setAttribute("loginMethod", "PASSWORD").createForm("hanko-multi-login.ftl");
                authenticationFlowContext.challenge(response);
                break;
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }
}
