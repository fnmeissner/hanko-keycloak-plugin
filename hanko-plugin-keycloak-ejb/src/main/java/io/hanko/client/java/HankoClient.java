package io.hanko.client.java;

import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;
import io.hanko.client.java.json.HankoJsonParser;
import io.hanko.client.java.json.HankoJsonParserFactory;
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

    public HankoRegistrationRequest requestRegistration(String userId, String username, String apiKey, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{ \"operation\": \"REG\", \"username\": \"" + username + "\", \"userId\": \"" + userId + "\", " +
                "\"clientData\": { \"remoteAddress\": \"" + remoteAddress + "\" }}";
        InputStream is = httpClient.post("/uaf/requests", json, apiKey);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRegistrationRequest requestDeregistration(String userId, String username, String apiKey) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{ \"operation\": \"DEREG\", \"username\": \"" + username + "\", \"userId\": \"" + userId + "\" }";
        InputStream is = httpClient.post("/uaf/requests", json, apiKey);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRequest requestAuthentication(String userId, String username, String apikey, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create();
        String json = "{ \"operation\": \"AUTH\", \"username\": \"" + username + "\", \"userId\": \"" + userId + "\", " +
                "\"clientData\": { \"remoteAddress\": \"" + remoteAddress + "\" }}";
        InputStream is = httpClient.post("/uaf/requests", json, apikey);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRequest awaitConfirmation(String id, String apiKey) {
        HankoHttpClient httpClient = httpClientFactory.create();
        InputStream is = httpClient.get("/requests/finished/" + id, apiKey);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }
}
