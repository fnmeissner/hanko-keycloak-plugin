package io.hanko.plugin.keycloak.questioning;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

import java.util.Collections;
import java.util.List;

public class UserQuestioningJpaEntityProvider implements JpaEntityProvider {
    @Override
    public List<Class<?>> getEntities() {
        return Collections.<Class<?>>singletonList(UserQuestioningRequestEntity.class);
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/user-questioning-changelog.xml";
    }

    @Override
    public String getFactoryId() {
        return UserQuestioningJpaEntityProviderFactory.ID;
    }

    @Override
    public void close() {

    }
}
