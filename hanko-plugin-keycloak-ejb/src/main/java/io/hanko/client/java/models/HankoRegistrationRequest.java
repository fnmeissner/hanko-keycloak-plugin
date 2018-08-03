package io.hanko.client.java.models;

public class HankoRegistrationRequest extends HankoRequest {
    public String getQrCodeLink() {
        return getLink("qrcode").href;
    }
}
