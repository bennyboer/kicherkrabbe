package de.bennyboer.kicherkrabbe.app;

public enum Profile {
    PRODUCTION,
    DEVELOPMENT;

    public boolean isProduction() {
        return this == PRODUCTION;
    }

    public boolean isDevelopment() {
        return this == DEVELOPMENT;
    }
}
