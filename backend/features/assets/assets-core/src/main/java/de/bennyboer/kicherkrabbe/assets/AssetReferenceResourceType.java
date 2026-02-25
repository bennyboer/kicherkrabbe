package de.bennyboer.kicherkrabbe.assets;

public enum AssetReferenceResourceType {

    FABRIC,
    PATTERN,
    PRODUCT,
    HIGHLIGHT,
    OFFER;

    public boolean isPubliclyAccessible() {
        return switch (this) {
            case FABRIC, PATTERN, HIGHLIGHT, OFFER -> true;
            case PRODUCT -> false;
        };
    }

}
