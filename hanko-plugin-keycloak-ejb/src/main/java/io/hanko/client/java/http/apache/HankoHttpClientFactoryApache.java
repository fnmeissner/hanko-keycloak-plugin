package io.hanko.client.java.http.apache;

import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;

public class HankoHttpClientFactoryApache implements HankoHttpClientFactory {
    private String baseUrl;

    public HankoHttpClientFactoryApache() {
        baseUrl = "https://api.hanko.io";
    }

    @Override
    public HankoHttpClient create() {
        return new HankoHttpClientApache(baseUrl);
    }
}
