package io.hanko.plugin.keycloak.serialization;

public class HankoRegistrationChallenge {
    public HankoRegistrationChallenge(String qrCode) {
        this.qrCode = qrCode;
    }

    public String qrCode;
}
