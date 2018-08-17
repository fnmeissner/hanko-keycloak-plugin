package io.hanko.client.java.http;

import java.io.InputStream;

public interface HankoHttpClient {
    InputStream post(String url, String json, String apiKey, String apiKeyId);
    InputStream get(String url, String apiKey, String apiKeyId);
    void close();
}
