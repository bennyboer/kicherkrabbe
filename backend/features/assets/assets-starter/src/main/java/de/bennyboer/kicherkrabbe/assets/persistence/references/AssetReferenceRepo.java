package de.bennyboer.kicherkrabbe.assets.persistence.references;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.AssetReference;
import de.bennyboer.kicherkrabbe.assets.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.AssetResourceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface AssetReferenceRepo {

    Mono<Void> upsert(AssetReference reference);

    Mono<Void> removeByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId);

    Flux<AssetReference> findByAssetId(AssetId assetId);

    Flux<AssetId> findAssetIdsByResourceNameContaining(String searchTerm);

    Flux<AssetReference> findByAssetIds(Collection<AssetId> assetIds);

    Flux<AssetReference> findByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId);

    Mono<Void> updateResourceName(AssetReferenceResourceType resourceType, AssetResourceId resourceId, String resourceName);

}
