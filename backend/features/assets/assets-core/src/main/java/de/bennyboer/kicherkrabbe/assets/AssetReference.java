package de.bennyboer.kicherkrabbe.assets;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class AssetReference {

    AssetId assetId;

    AssetReferenceResourceType resourceType;

    AssetResourceId resourceId;

    public static AssetReference of(AssetId assetId, AssetReferenceResourceType resourceType, AssetResourceId resourceId) {
        notNull(assetId, "Asset ID must be given");
        notNull(resourceType, "Resource type must be given");
        notNull(resourceId, "Resource ID must be given");

        return new AssetReference(assetId, resourceType, resourceId);
    }

}
