package io.hanko.client.java.http;

import io.hanko.client.java.HankoClientConfig;

public interface HankoHttpClientFactory {
    public HankoHttpClient create(HankoClientConfig config);
}
