package io.hanko.plugin.keycloak;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ServicesLogger;

class HankoUtils {
    static String CONFIG_APIKEY = "hanko.apikey";

    private static ServicesLogger logger = ServicesLogger.LOGGER;

    static String getApiKey(KeycloakSession session) throws HankoConfigurationException {
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

        for (AuthenticatorConfigModel config : session.getContext().getRealm().getAuthenticatorConfigs()) {
            if (config.getConfig().containsKey(CONFIG_APIKEY)) {
                return getApiKey(config);
            }
        }

        throw new HankoConfigurationException("Could not find Hanko apikey. " +
                "Please set the Hanko apikey in the configuration for the Hanko UAF Authenticator " +
                "in your authentication flow.");
    }

    static String getApiKey(AuthenticatorConfigModel config) throws HankoConfigurationException {
        String apikey = config.getConfig().get(CONFIG_APIKEY);
        if(apikey == null || apikey.trim().isEmpty()) {
            throw new HankoConfigurationException("Could not find Hanko apikey. " +
                    "Please set the Hanko apikey in the configuration for the Hanko UAF Authenticator " +
                    "in your authentication flow.");
        }
        return apikey;
    }
}
