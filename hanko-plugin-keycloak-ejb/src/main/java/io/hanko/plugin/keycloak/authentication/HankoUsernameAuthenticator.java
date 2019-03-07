package io.hanko.plugin.keycloak.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.LoginBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class HankoUsernameAuthenticator extends AbstractUsernameFormAuthenticator implements Authenticator {
    protected static ServicesLogger log = ServicesLogger.LOGGER;

    public HankoUsernameAuthenticator() {}

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String authNote = context.getAuthenticationSession().getAuthNote("TEST");
        log.error("authNote: " + authNote);
        LoginFormsProvider forms = context.form();
        forms.setAttribute("login",  new LoginBean(context.getHttpRequest().getDecodedFormParameters()));
        Response challengeResponse = forms.createForm("hanko-username.ftl");
        if(context.getUser() != null) {
            log.error("getUser != null => attempted");
            context.attempted();
        } else {
            log.error("getUser == null => challenge");
            context.challenge(challengeResponse);
        }
    }

    protected Response challenge(AuthenticationFlowContext context, String error) {
        LoginFormsProvider forms = context.form();
        forms.setAttribute("login",  new LoginBean(context.getHttpRequest().getDecodedFormParameters()));
        if (error != null) {
            forms.setError(error, new Object[0]);
        }
        return forms.createForm("hanko-username.ftl");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (!validateUserAndPassword(context, formData)) {
            return;
        }

        context.success();
    }

    @Override
    public boolean validateUserAndPassword(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, Messages.INVALID_USER);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return false;
        }

        // remove leading and trailing whitespace
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // Could happen during federation import
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }

            return false;
        }

        if (invalidUser(context, user)) {
            return false;
        }

//        if (!validatePassword(context, user, inputData)) {
//            return false;
//        }

        if (!enabledUser(context, user)) {
            return false;
        }



        String rememberMe = inputData.getFirst("rememberMe");
        boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }

        context.setUser(user);
        context.success();

        return true;
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        // nothing to do here
    }
}
