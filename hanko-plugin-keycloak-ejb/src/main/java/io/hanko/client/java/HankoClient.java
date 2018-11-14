package io.hanko.client.java;

import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;
import io.hanko.client.java.json.HankoJsonParser;
import io.hanko.client.java.json.HankoJsonParserFactory;
import io.hanko.client.java.models.HankoDevice;
import io.hanko.client.java.models.HankoRegistrationRequest;
import io.hanko.client.java.models.HankoRequest;
import org.keycloak.services.ServicesLogger;

import java.io.InputStream;

public class HankoClient {
    private HankoHttpClientFactory httpClientFactory;
    private HankoJsonParser jsonParser;
    protected static ServicesLogger log = ServicesLogger.LOGGER;

    public HankoClient(HankoHttpClientFactory httpClientFactory, HankoJsonParserFactory jsonParserFactory) {
        this.httpClientFactory = httpClientFactory;
        this.jsonParser = jsonParserFactory.create();
    }

    public HankoRegistrationRequest requestRegistration(HankoClientConfig config, String userId, String username, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        String json = "{\"operation\":\"REG\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"," +
                "\"clientData\":{\"remoteAddress\":\"" + remoteAddress + "\"}}";
        InputStream is = httpClient.post("/v1/uaf/requests", json);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRegistrationRequest requestDeregistration(HankoClientConfig config, String userId, String username) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        String json = "{\"operation\":\"DEREG\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"}";
        InputStream is = httpClient.post("/v1/uaf/requests", json);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public Boolean hasRegisteredDevices(HankoClientConfig config, String userId) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        InputStream is = httpClient.get("/mgmt/v1/registrations/" + userId);
        HankoDevice[] devices = jsonParser.parse(is, HankoDevice[].class);
        return devices.length > 0;
    }


    public HankoDevice[] getRegisteredDevices(HankoClientConfig config, String userId) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        InputStream is = httpClient.get("/mgmt/v1/registrations/" + userId);
        HankoDevice[] devices = jsonParser.parse(is, HankoDevice[].class);
        return devices;
    }

    public HankoRequest deleteDevice(HankoClientConfig config, String userId, String username, String deviceId) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        String json = "{\"operation\":\"DEREG\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\",\"deviceIds\":[\"" + deviceId + "\"]}";
        log.error("sending json: " + json);
        InputStream is = httpClient.post("/v1/uaf/requests", json);
        HankoRegistrationRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRequest requestAuthentication(HankoClientConfig config, String userId, String username, String remoteAddress) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        String json = "{\"operation\":\"AUTH\",\"username\":\"" + username + "\",\"userId\":\"" + userId + "\"," +
                "\"clientData\":{\"remoteAddress\":\"" + remoteAddress + "\"}}";
        InputStream is = httpClient.post("/v1/uaf/requests", json);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRequest.class);
        httpClient.close();
        return hankoRequest;
    }

    public HankoRequest awaitConfirmation(HankoClientConfig config, String id) {
        HankoHttpClient httpClient = httpClientFactory.create(config);
        InputStream is = httpClient.get("/v1/uaf/requests/" + id);
        HankoRequest hankoRequest = jsonParser.parse(is, HankoRegistrationRequest.class);
        httpClient.close();
        return hankoRequest;
    }
}
