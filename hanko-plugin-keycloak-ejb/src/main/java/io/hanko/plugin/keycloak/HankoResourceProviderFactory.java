package io.hanko.plugin.keycloak;

import io.hanko.client.java.HankoClient;
import io.hanko.client.java.http.HankoHttpClientFactory;
import io.hanko.client.java.http.apache.HankoHttpClientFactoryApache;
import io.hanko.client.java.json.HankoJsonParserFactory;
import io.hanko.client.java.json.jackson.HankoJsonParserFactoryJackson;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.theme.FreeMarkerUtil;

public class HankoResourceProviderFactory implements RealmResourceProviderFactory //, ConfiguredProvider
{
    public static final String ID = "hanko";

    protected static ServicesLogger log = ServicesLogger.LOGGER;

    private FreeMarkerUtil freeMarker;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        HankoHttpClientFactory httpClientFactory =
                new HankoHttpClientFactoryApache();
        HankoJsonParserFactory jsonParserFactory = new HankoJsonParserFactoryJackson();
        HankoClient hankoClient = new HankoClient(httpClientFactory, jsonParserFactory);

        HankoResourceProvider provider = new HankoResourceProvider(session, hankoClient, freeMarker);
        ResteasyProviderFactory.getInstance().injectProperties(provider);
        return provider;
    }

    @Override
    public void init(Config.Scope config) {
        freeMarker = new FreeMarkerUtil();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        freeMarker = null;
    }
}
