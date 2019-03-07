package io.hanko.client.java;

public class HankoClientConfig {
    public HankoClientConfig(String apiUrl, String apiKeyId, String apiKeySecret, Boolean requries2fa) {
        this.apiUrl = apiUrl;
        this.apiKeyId = apiKeyId;
        this.apiKeySecret = apiKeySecret;
        this.requries2fa = requries2fa;
        this.isProxyEnabled = false;
        this.proxyAddress = null;
        this.proxyPort = null;
        this.proxyType = null;
    }

    public HankoClientConfig(String apiUrl, String apiKeyId, String apiKeySecret, Boolean requries2fa, String proxyAddress, String proxyPort, String proxyType) {
        this.apiUrl = apiUrl;
        this.apiKeyId = apiKeyId;
        this.apiKeySecret = apiKeySecret;
        this.requries2fa = requries2fa;
        this.isProxyEnabled = true;
        this.proxyAddress = proxyAddress;
        this.proxyPort = Integer.parseInt(proxyPort);
        this.proxyType = proxyType;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKeyId() {
        return apiKeyId;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public Boolean getRequries2fa() { return requries2fa; }

    public Boolean getProxyEnabled() {
        return isProxyEnabled;
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getProxyType() {
        return proxyType;
    }

    private String apiUrl;
    private String apiKeyId;
    private String apiKeySecret;
    private Boolean requries2fa;
    private Boolean isProxyEnabled;
    private String proxyAddress;
    private Integer proxyPort;
    private String proxyType;
}
