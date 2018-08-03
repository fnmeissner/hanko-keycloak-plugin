package io.hanko.plugin.keycloak.serialization;

public class HankoStatus {
    public HankoStatus(boolean isPasswordlessActive) {
        this.isPasswordlessActive = isPasswordlessActive;
    }

    public boolean isPasswordlessActive;
}
