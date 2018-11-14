package io.hanko.client.java.http;

import java.io.InputStream;

public interface HankoHttpClient {
    InputStream post(String url, String json);
    InputStream get(String url);
    void close();
}
