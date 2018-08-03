package io.hanko.plugin.keycloak;

public class HankoConfigurationException extends Exception {
    public HankoConfigurationException() {
    }

    public HankoConfigurationException(String message) {
        super(message);
    }

    public HankoConfigurationException(Throwable cause) {
        super(cause);
    }

    public HankoConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
