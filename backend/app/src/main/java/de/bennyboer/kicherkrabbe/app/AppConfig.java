package de.bennyboer.kicherkrabbe.app;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppConfig {

    @Builder.Default
    boolean isSecure = true;

    @Builder.Default
    String host = "0.0.0.0";

    @Builder.Default
    int port = 443;

    @Builder.Default
    Profile profile = Profile.PRODUCTION;

    @Nullable
    @Builder.Default
    String certPath = null;

    @Nullable
    @Builder.Default
    String keyPath = null;

    public boolean isDevelopmentProfile() {
        return profile.isDevelopment();
    }

    public boolean isProductionProfile() {
        return profile.isProduction();
    }

    public Optional<String> getCertPath() {
        return Optional.ofNullable(certPath);
    }

    public Optional<String> getKeyPath() {
        return Optional.ofNullable(keyPath);
    }

}
