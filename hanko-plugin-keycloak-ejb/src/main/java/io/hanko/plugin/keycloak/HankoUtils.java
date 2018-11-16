package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClientConfig;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Formatter;

public class HankoUtils {
    static String CONFIG_API_URL = "hanko.apiurl";
    static String CONFIG_APIKEY = "hanko.apikey";
    static String CONFIG_APIKEYID = "hanko.apikeyid";
    static String CONFIG_HAS_PROXY = "hanko.hasproxy";
    static String CONFIG_PROXY_ADDRESS = "hanko.proxyaddress";
    static String CONFIG_PROXY_PORT = "hanko.proxyport";
    static String CONFIG_PROXY_TYPE= "hanko.proxytype";
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

    static HankoClientConfig createConfig(KeycloakSession session) throws HankoConfigurationException  {
        String apiUrl = getApiUrl(session);
        String apiKeyId = getApiKeyId(session);
        String apiKeySecret = getApiKey(session);

        Boolean apiHasProxy = getHasProxy(session);

        if(apiHasProxy) {
            String proxyAddress = getProxyAddress(session);
            String proxyPort = getProxyPort(session);
            String proxyType = getProxyType(session);
            return new HankoClientConfig(apiUrl, apiKeyId, apiKeySecret, proxyAddress, proxyPort, proxyType);
        } else {
            return new HankoClientConfig(apiUrl, apiKeyId, apiKeySecret);
        }
    }

    static String getApiUrl(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_API_URL, "API url");
    }

    static String getApiKey(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_APIKEY, "API key secret");
    }

    static String getApiKeyId(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_APIKEYID, "API key id");
    }

    static Boolean getHasProxy(KeycloakSession session) throws HankoConfigurationException {
        String value = getNullableConfigValue(session, CONFIG_HAS_PROXY, "Has Proxy");
        if(value == null) {
            value = "false";
        }
        return Boolean.parseBoolean(value);
    }

    static String getProxyAddress(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_PROXY_ADDRESS, "API proxy address");
    }

    static String getProxyPort(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_PROXY_PORT, "API proxy port");
    }

    static String getProxyType(KeycloakSession session) throws HankoConfigurationException {
        return getNonEmptyConfigValue(session, CONFIG_PROXY_TYPE, "API proxy type");
    }

    static String getApiUrl(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_API_URL, "API url");
    }

    static String getApiKey(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_APIKEY, "API key secret");
    }

    static String getApiKeyId(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_APIKEYID, "API key id");
    }

    static Boolean getHasProxy(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return Boolean.parseBoolean(getNonEmptyConfigValue(config, CONFIG_HAS_PROXY, "API proxy enabled"));
    }

    static String getProxyAddress(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_PROXY_ADDRESS, "API proxy address");
    }

    static String getProxyPort(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_PROXY_PORT, "API proxy port");
    }

    static String getProxyType(AuthenticatorConfigModel config) throws HankoConfigurationException {
        return getNonEmptyConfigValue(config, CONFIG_PROXY_TYPE, "API proxy type");
    }

    private static String getNonEmptyConfigValue(KeycloakSession session, String key, String description) throws HankoConfigurationException {
        verifySession(session);

        for (AuthenticatorConfigModel config : session.getContext().getRealm().getAuthenticatorConfigs()) {
            if (config.getConfig().containsKey(key)) {
                return getNonEmptyConfigValue(config, key, description);
            }
        }

        return throwHankoConfigException(key, description);
    }

    private static String getNullableConfigValue(KeycloakSession session, String key, String description) throws HankoConfigurationException {
        verifySession(session);

        for (AuthenticatorConfigModel config : session.getContext().getRealm().getAuthenticatorConfigs()) {
            if (config.getConfig().containsKey(key)) {
                return getNullableConfigValue(config, key, description);
            }
        }

        return null;
    }

    private static String getNonEmptyConfigValue(AuthenticatorConfigModel config, String key, String description) throws HankoConfigurationException {
        String value = config.getConfig().get(key);
        if(key == null || key.trim().isEmpty()) {
            throwHankoConfigException(key, description);
        }
        return value;
    }

    private static String getNullableConfigValue(AuthenticatorConfigModel config, String key, String description) throws HankoConfigurationException {
        return config.getConfig().get(key);
    }

    private static String throwHankoConfigException(String key, String description) throws HankoConfigurationException {
        throw new HankoConfigurationException("Could not find " + description + ". " +
                "Please set its value in the configuration for the Hanko UAF Authenticator " +
                "in your authentication flow.");
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
