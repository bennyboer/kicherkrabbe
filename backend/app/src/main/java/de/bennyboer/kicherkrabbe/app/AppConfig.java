package de.bennyboer.kicherkrabbe.app;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AppConfig {

    @Builder.Default
    boolean isSecure = true;

    @Builder.Default
    String host = "0.0.0.0";

    @Builder.Default
    int port = 7070;

    @Builder.Default
    Profile profile = Profile.PRODUCTION;

    public boolean isDevelopmentProfile() {
        return profile.isDevelopment();
    }

    public boolean isProductionProfile() {
        return profile.isProduction();
    }
    
}
