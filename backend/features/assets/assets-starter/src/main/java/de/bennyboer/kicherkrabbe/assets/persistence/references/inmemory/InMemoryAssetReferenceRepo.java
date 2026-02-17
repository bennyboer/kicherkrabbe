package de.bennyboer.kicherkrabbe.assets.persistence.references.inmemory;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReference;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetResourceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAssetReferenceRepo implements AssetReferenceRepo {

    private final Map<String, AssetReference> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> upsert(AssetReference reference) {
        return Mono.fromCallable(() -> {
            lookup.put(toKey(reference), reference);
            return null;
        });
    }

    @Override
    public Mono<Void> removeByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId) {
        return Mono.fromCallable(() -> {
            lookup.entrySet().removeIf(entry -> {
                var ref = entry.getValue();
                return ref.getResourceType() == resourceType
                        && ref.getResourceId().equals(resourceId);
            });
            return null;
        });
    }

    @Override
    public Flux<AssetReference> findByAssetId(AssetId assetId) {
        return Flux.fromIterable(lookup.values())
                .filter(ref -> ref.getAssetId().equals(assetId));
    }

    private String toKey(AssetReference reference) {
        return reference.getAssetId().getValue()
                + "_" + reference.getResourceType().name()
                + "_" + reference.getResourceId().getValue();
    }

}
