package io.hanko.plugin.keycloak.questioning;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserQuestioningJpaEntityProviderFactory implements JpaEntityProviderFactory {
    protected static final String ID = "hanko-user-questioning-entity-provider";

    @Override
    public JpaEntityProvider create(KeycloakSession keycloakSession) {
        return new UserQuestioningJpaEntityProvider();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }
}
