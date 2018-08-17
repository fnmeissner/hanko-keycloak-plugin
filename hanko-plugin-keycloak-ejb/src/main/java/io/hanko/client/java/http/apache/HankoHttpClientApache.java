package io.hanko.client.java.http.apache;

import io.hanko.client.java.HmacUtil;
import io.hanko.client.java.http.HankoHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class HankoHttpClientApache implements HankoHttpClient {
    private String baseUrl;
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private final HmacUtil hmacUtil;

    public HankoHttpClientApache(String baseUrl) {
        this.baseUrl = baseUrl;
        this.hmacUtil = new HmacUtil();
    }

    @Override
    public InputStream post(String url, String json, String apiKey, String apiKeyId) {
        // create request
        HttpPost httpPost = new HttpPost(baseUrl + url);
        httpPost.setHeader("Content-Type", "application/json");

        // add json body
        try {
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            String path = httpPost.getURI().getPath();
            String method = httpPost.getMethod();
            String authHeader = hmacUtil.makeAuthorizationHeader(apiKey, apiKeyId, method, path, json);
            httpPost.addHeader("Authorization", authHeader);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return executeRequest(httpPost);
    }

    @Override
    public InputStream get(String url, String apiKey, String apiKeyId) {
        // create request
        HttpGet httpGet = new HttpGet(baseUrl + url);
        try {
            String path = httpGet.getURI().getPath();
            String method = httpGet.getMethod();
            String authHeader = hmacUtil.makeAuthorizationHeader(apiKey, apiKeyId, method, path, "");
            httpGet.addHeader("Authorization", authHeader);
            return executeRequest(httpGet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private InputStream executeRequest(HttpUriRequest request) {
        try {
            CloseableHttpResponse response = httpclient.execute(request);
            HttpEntity resultEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 401) {
                String content = new BufferedReader(new InputStreamReader(resultEntity.getContent()))
                        .lines().collect(Collectors.joining("\n"));
                throw new RuntimeException("Hanko API returned 401, please check your API key configuration: " + content);
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
