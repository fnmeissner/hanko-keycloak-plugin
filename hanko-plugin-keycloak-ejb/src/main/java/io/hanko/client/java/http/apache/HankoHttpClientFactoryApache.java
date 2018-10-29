package io.hanko.client.java.http.apache;

import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;

public class HankoHttpClientFactoryApache implements HankoHttpClientFactory {
    @Override
    public HankoHttpClient create(String baseUrl) {
        return new HankoHttpClientApache(baseUrl);
    }
}
