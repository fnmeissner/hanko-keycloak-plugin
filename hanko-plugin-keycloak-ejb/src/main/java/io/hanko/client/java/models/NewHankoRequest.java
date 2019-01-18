package io.hanko.client.java.models;

public class NewHankoRequest {
    public String operation;
    public String username;
    public String userId;
    public String transaction;
    public String[] deviceIds;
    public ClientData clientData;

    public NewHankoRequest withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public NewHankoRequest withUsername(String username) {
        this.username = username;
        return this;
    }

    public NewHankoRequest withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public NewHankoRequest withTransaction(String transaction) {
        this.transaction = transaction;
        return this;
    }

    public NewHankoRequest withDeviceId(String deviceId) {
        this.deviceIds = new String[] { deviceId };
        return this;
    }

    public NewHankoRequest withClientData(ClientData clientData) {
        this.clientData = clientData;
        return this;
    }
}
