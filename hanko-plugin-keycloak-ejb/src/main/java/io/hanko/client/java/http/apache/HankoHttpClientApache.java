package io.hanko.client.java.http.apache;

import io.hanko.client.java.http.HankoHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class HankoHttpClientApache implements HankoHttpClient {
    private String baseUrl;
    private CloseableHttpClient httpclient = HttpClients.createDefault();

    public HankoHttpClientApache(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public InputStream post(String url, String json, String apiKey) {
        // create request
        HttpPost httpPost = new HttpPost(baseUrl + url);
        httpPost.setHeader("Content-Type", "application/json");

        // add json body
        try {
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        return executeRequest(httpPost, apiKey);
    }

    @Override
    public InputStream get(String url, String apiKey) {
        // create request
        HttpGet httpGet = new HttpGet(baseUrl + url);
        return executeRequest(httpGet, apiKey);
    }

    private InputStream executeRequest(HttpUriRequest request, String apiKey) {
        try {
            request.setHeader("X-Authorization", "apiKey=" + apiKey);

            CloseableHttpResponse response = httpclient.execute(request);
            HttpEntity resultEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 401) {
                throw new RuntimeException("Hanko API returned 401, please check your API key configuration");
            }

            return resultEntity.getContent();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
