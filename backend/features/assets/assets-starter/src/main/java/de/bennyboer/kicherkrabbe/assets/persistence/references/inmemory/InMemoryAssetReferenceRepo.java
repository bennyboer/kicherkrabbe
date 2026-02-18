package de.bennyboer.kicherkrabbe.assets.persistence.references.inmemory;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.AssetReference;
import de.bennyboer.kicherkrabbe.assets.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.AssetResourceId;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Locale;
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

    @Override
    public Flux<AssetId> findAssetIdsByResourceNameContaining(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase(Locale.ROOT);

        return Flux.fromIterable(lookup.values())
                .filter(ref -> ref.getResourceName().toLowerCase(Locale.ROOT).contains(lowerSearchTerm))
                .map(AssetReference::getAssetId)
                .distinct();
    }

    @Override
    public Flux<AssetReference> findByAssetIds(Collection<AssetId> assetIds) {
        return Flux.fromIterable(lookup.values())
                .filter(ref -> assetIds.contains(ref.getAssetId()));
    }

    @Override
    public Flux<AssetReference> findByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId) {
        return Flux.fromIterable(lookup.values())
                .filter(ref -> ref.getResourceType() == resourceType && ref.getResourceId().equals(resourceId));
    }

    @Override
    public Mono<Void> updateResourceName(
            AssetReferenceResourceType resourceType,
            AssetResourceId resourceId,
            String resourceName
    ) {
        return Mono.fromCallable(() -> {
            lookup.replaceAll((key, ref) -> {
                if (ref.getResourceType() == resourceType && ref.getResourceId().equals(resourceId)) {
                    return AssetReference.of(ref.getAssetId(), ref.getResourceType(), ref.getResourceId(), resourceName);
                }
                return ref;
            });
            return null;
        });
    }

    private String toKey(AssetReference reference) {
        return reference.getAssetId().getValue()
                + "_" + reference.getResourceType().name()
                + "_" + reference.getResourceId().getValue();
    }

}
