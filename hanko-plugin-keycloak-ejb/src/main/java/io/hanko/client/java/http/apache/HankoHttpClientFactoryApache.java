package io.hanko.client.java.http.apache;

import io.hanko.client.java.HankoClientConfig;
import io.hanko.client.java.http.HankoHttpClient;
import io.hanko.client.java.http.HankoHttpClientFactory;

public class HankoHttpClientFactoryApache implements HankoHttpClientFactory {
    @Override
    public HankoHttpClient create(HankoClientConfig config) {
        return new HankoHttpClientApache(config);
    }
}
