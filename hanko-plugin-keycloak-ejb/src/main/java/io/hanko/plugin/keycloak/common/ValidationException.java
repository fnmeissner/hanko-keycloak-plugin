package io.hanko.plugin.keycloak.common;

public class ValidationException extends Exception {
    public ValidationException() {}

    public ValidationException(String message) {
        super(message);
    }
}
