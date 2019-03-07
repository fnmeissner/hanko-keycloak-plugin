package io.hanko.plugin.keycloak.serialization;

public class HankoRealmConfig {
    public HankoRealmConfig(boolean requires2fa) {
        this.requires2fa = requires2fa;
    }

    public boolean requires2fa;
}
