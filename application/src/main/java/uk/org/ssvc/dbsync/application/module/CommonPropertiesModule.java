package uk.org.ssvc.dbsync.application.module;


import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Module
public class CommonPropertiesModule {

    @Provides
    @Named("encryption.key")
    String encryptionKey() {
        return prop("encryption.key");
    }

    @Provides
    @Named("encryption.password")
    String encryptionPassword() {
        return prop("encryption.password");
    }

    @Provides
    @Named("ssvc.members.url")
    String memberUrl() {
        return prop("ssvc.members.url");
    }

    @Provides
    @Named("ssvc.members.username")
    String memberUsername() {
        return prop("ssvc.members.username");
    }

    @Provides
    @Named("ssvc.members.password")
    String memberPassword() {
        return prop("ssvc.members.password");
    }

    @Provides
    @Named("google.clientId")
    String googleClientId() {
        return prop("google.clientId");
    }

    @Provides
    @Named("google.clientEmail")
    String googleClientEmail() {
        return prop("google.clientEmail");
    }

    @Provides
    @Named("google.privateKey")
    String googlePrivateKey() {
        return prop("google.privateKey");
    }

    @Provides
    @Named("google.privateKeyId")
    String googlePrivateKeyId() {
        return prop("google.privateKeyId");
    }

    @Provides
    @Named("google.projectId")
    String googleProjectId() {
        return prop("google.projectId");
    }

    private String prop(String key) {
        return defaultIfBlank(
            System.getenv(key),
            System.getProperty(key));
    }

}
