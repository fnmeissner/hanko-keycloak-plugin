package io.hanko.plugin.keycloak;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Formatter;

public class HankoUtils {
    static String CONFIG_APIKEY = "hanko.apikey";
    static String CONFIG_APIKEYID = "hanko.apikeyid";
    static String AUTH_NOTE_IS_USER_AUTHENTICATED = "HANKO_REQUIRED";

    private static ServicesLogger logger = ServicesLogger.LOGGER;

    private static void verifySession(KeycloakSession session) throws HankoConfigurationException {
        if (session == null) {
            throw new HankoConfigurationException("Could not find Hanko apikey because the session is null. " +
                    "There might be a problem with you authentication flow configuration.");
        }

        if (session.getContext() == null) {
            throw new HankoConfigurationException("Could not find Hanko apikey because the context is null. " +
                    "There might be a problem with you authentication flow configuration.");
        }

        if (session.getContext().getRealm() == null) {
            throw new HankoConfigurationException("Could not find Hanko apikey because the realm is null. " +
                    "There might be a problem with you authentication flow configuration.");
        }
    }

    static String getApiKey(KeycloakSession session) throws HankoConfigurationException {
        verifySession(session);

        for (AuthenticatorConfigModel config : session.getContext().getRealm().getAuthenticatorConfigs()) {
            if (config.getConfig().containsKey(CONFIG_APIKEY)) {
                return getApiKey(config);
            }
        }

        throw new HankoConfigurationException("Could not find Hanko apikey. " +
                "Please set the Hanko apikey in the configuration for the Hanko UAF Authenticator " +
                "in your authentication flow.");
    }

    static String getApiKeyId(KeycloakSession session) throws HankoConfigurationException {
        verifySession(session);

        for (AuthenticatorConfigModel config : session.getContext().getRealm().getAuthenticatorConfigs()) {
            if (config.getConfig().containsKey(CONFIG_APIKEYID)) {
                return getApiKeyId(config);
            }
        }

        throw new HankoConfigurationException("Could not find Hanko apikey. " +
                "Please set the Hanko apikey in the configuration for the Hanko UAF Authenticator " +
                "in your authentication flow.");
    }

    static String getApiKey(AuthenticatorConfigModel config) throws HankoConfigurationException {
        String apikey = config.getConfig().get(CONFIG_APIKEY);
        if (apikey == null || apikey.trim().isEmpty()) {
            throw new HankoConfigurationException("Could not find Hanko apikey. " +
                    "Please set the Hanko apikey in the configuration for the Hanko UAF Authenticator " +
                    "in your authentication flow.");
        }
        return apikey;
    }


    static String getApiKeyId(AuthenticatorConfigModel config) throws HankoConfigurationException {
        String apiKeyId = config.getConfig().get(CONFIG_APIKEYID);
        if (apiKeyId == null || apiKeyId.trim().isEmpty()) {
            throw new HankoConfigurationException("Could not find Hanko apikey id. " +
                    "Please set the Hanko apikey id in the configuration for the Hanko UAF Authenticator " +
                    "in your authentication flow.");
        }
        return apiKeyId;
    }

    static void setIsUserAuthenticated(AuthenticationSessionModel authSession) {
        authSession.setAuthNote(AUTH_NOTE_IS_USER_AUTHENTICATED, Boolean.toString(true));
    }

    static boolean isUserAuthenticated(AuthenticationSessionModel authSession) {
        String authNote = authSession.getAuthNote(AUTH_NOTE_IS_USER_AUTHENTICATED);
        try {
            return Boolean.parseBoolean(authNote);
        } catch (Exception ex) {
            logger.error("Could not read auth-note " + AUTH_NOTE_IS_USER_AUTHENTICATED, ex);
            return true;
        }
    }

    static void removeHankoRequiredAuthNote(AuthenticationSessionModel authSession) {
        authSession.removeAuthNote(AUTH_NOTE_IS_USER_AUTHENTICATED);
    }

    public static String asHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
