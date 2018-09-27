package io.hanko.client.java;

import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;
import io.hanko.client.java.json.HankoJsonParser;
import io.hanko.client.java.json.HankoJsonParserFactory;
import io.hanko.client.java.models.HankoDevice;
import io.hanko.client.java.models.HankoRegistrationRequest;
import io.hanko.client.java.models.HankoRequest;

import java.io.InputStream;

public class HankoClient {
    private HankoHttpClientFactory httpClientFactory;
    private HankoJsonParser jsonParser;

    public HankoClient(HankoHttpClientFactory httpClientFactory, HankoJsonParserFactory jsonParserFactory) {
        this.httpClientFactory = httpClientFactory;
        this.jsonParser = jsonParserFactory.create();
    }

    public HankoRegistrationRequest requestRegistration(String userId, String username, String apiKey, String apiKeyId, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{\"operation\":\"REG\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"," +
                "\"clientData\":{\"remoteAddress\":\"" + remoteAddress + "\"}}";
        InputStream is = httpClient.post("/v1/uaf/requests", json, apiKey, apiKeyId);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRegistrationRequest requestDeregistration(String userId, String username, String apiKey, String apiKeyId) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{\"operation\":\"DEREG\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"}";
        InputStream is = httpClient.post("/v1/uaf/requests", json, apiKey, apiKeyId);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public Boolean hasRegisteredDevices(String userId, String apiKey, String apiKeyId) {
        HankoHttpClient httpClient = httpClientFactory.create();
        InputStream is = httpClient.get("/mgmt/v1/registrations/" + userId, apiKey, apiKeyId);
        HankoDevice[] devices = jsonParser.parse(is, HankoDevice[].class);
        return devices.length > 0;
    }

    public HankoRequest requestAuthentication(String userId, String username, String apikey, String apiKeyId, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{\"operation\":\"AUTH\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"," +
                "\"clientData\":{\"remoteAddress\":\"" + remoteAddress + "\"}}";
        InputStream is = httpClient.post("/v1/uaf/requests", json, apikey, apiKeyId);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRequest awaitConfirmation(String id, String apiKey, String apiKeyId) {
        HankoHttpClient httpClient = httpClientFactory.create();
        InputStream is = httpClient.get("/v1/uaf/requests/" + id, apiKey, apiKeyId);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }
}
