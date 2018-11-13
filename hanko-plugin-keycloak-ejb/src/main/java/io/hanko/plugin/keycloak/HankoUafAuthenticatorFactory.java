package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.http.HankoHttpClientFactory;
import io.hanko.client.java.http.apache.HankoHttpClientFactoryApache;
import io.hanko.client.java.json.HankoJsonParserFactory;
import io.hanko.client.java.json.jackson.HankoJsonParserFactoryJackson;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HankoUafAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {
    public static final String ID = "hanko-uaf-login";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public Authenticator create(KeycloakSession session) {
        HankoUserStore userStore = new HankoUserStore();
        HankoHttpClientFactory httpClientFactory = new HankoHttpClientFactoryApache();
        HankoJsonParserFactory jsonParserFactory = new HankoJsonParserFactoryJackson();
        HankoClient hankoClient = new HankoClient(httpClientFactory, jsonParserFactory);

        return new HankoUafAuthenticator(userStore, hankoClient);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getReferenceCategory() {
        return "hanko";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Hanko UAF Auth";
    }

    @Override
    public String getHelpText() {
        return "Hanko UAF Auth";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_API_URL);
        property.setLabel("Hanko API URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("https://api.hanko.io");
        property.setHelpText("Please use \"https://api.hanko.io\".");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_APIKEYID);
        property.setLabel("Hanko API KEY ID");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("Hanko API KEY ID.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_APIKEY);
        property.setLabel("Hanko API KEY");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("Hanko API KEY.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_HAS_PROXY);
        property.setLabel("Use Proxy server");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Use Proxy server.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_PROXY_ADDRESS);
        property.setLabel("Proxy address");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Proxy address");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_PROXY_PORT);
        property.setLabel("Proxy port");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Proxy port");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(HankoUtils.CONFIG_PROXY_TYPE);
        property.setLabel("Proxy type");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        List<String> options = new LinkedList<String>();
        options.add("http");
        options.add("https");
        property.setOptions(options);
        property.setHelpText("Proxy type");
        configProperties.add(property);
    }
}

