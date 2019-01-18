package io.hanko.plugin.keycloak.questioning;

import io.hanko.client.java.HankoClient;
import io.hanko.plugin.keycloak.common.HankoUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class UserQuestioningProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "hanko-questioning-provider-factory";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        HankoClient hankoClient = HankoUtils.createHankoClient();
        UserQuestioningProvider provider = new UserQuestioningProvider(session, hankoClient);
        ResteasyProviderFactory.getInstance().injectProperties(provider);
        return provider;
    }

    @Override
    public void init(Config.Scope scope) {}

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {}

    @Override
    public void close() {}

    @Override
    public String getId() {
        return ID;
    }
}
