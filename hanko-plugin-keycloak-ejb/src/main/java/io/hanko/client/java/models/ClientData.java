package io.hanko.client.java.models;

public class ClientData {
    public ClientData() {}

    public ClientData(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String remoteAddress;
    public String userAgent;
}
