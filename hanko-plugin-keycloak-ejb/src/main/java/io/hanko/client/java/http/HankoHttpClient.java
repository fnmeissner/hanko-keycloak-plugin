package io.hanko.client.java.http;

import java.io.InputStream;

public interface HankoHttpClient {
    InputStream post(String url, String json, String apiKey);
    InputStream get(String url, String apiKey);
    void close();
}
