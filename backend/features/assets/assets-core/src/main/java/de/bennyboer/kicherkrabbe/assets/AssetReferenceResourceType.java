package de.bennyboer.kicherkrabbe.assets;

public enum AssetReferenceResourceType {

    FABRIC,
    PATTERN,
    PRODUCT,
    HIGHLIGHT,
    OFFER,
    EMBROIDERY;

    public boolean isPubliclyAccessible() {
        return switch (this) {
            case FABRIC, PATTERN, HIGHLIGHT, OFFER, EMBROIDERY -> true;
            case PRODUCT -> false;
        };
    }

}
