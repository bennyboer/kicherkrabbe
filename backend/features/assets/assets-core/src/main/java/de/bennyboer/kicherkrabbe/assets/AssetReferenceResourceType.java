package de.bennyboer.kicherkrabbe.assets;

public enum AssetReferenceResourceType {

    FABRIC,
    PATTERN,
    PRODUCT,
    HIGHLIGHT;

    public boolean isPubliclyAccessible() {
        return switch (this) {
            case FABRIC, PATTERN, HIGHLIGHT -> true;
            case PRODUCT -> false;
        };
    }

}
