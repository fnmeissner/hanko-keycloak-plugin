package io.hanko.plugin.keycloak.serialization;

public class HankoRegistrationChallenge {
    public HankoRegistrationChallenge(String id, String qrCode, String request) {
        this.id = id;
        this.qrCode = qrCode;
        this.request = request;
    }

    public String id;
    public String qrCode;
    public String request;
}
