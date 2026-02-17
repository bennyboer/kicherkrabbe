package de.bennyboer.kicherkrabbe.assets.persistence.references;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetReferenceRepo {

    Mono<Void> upsert(AssetReference reference);

    Mono<Void> removeByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId);

    Flux<AssetReference> findByAssetId(AssetId assetId);

}
