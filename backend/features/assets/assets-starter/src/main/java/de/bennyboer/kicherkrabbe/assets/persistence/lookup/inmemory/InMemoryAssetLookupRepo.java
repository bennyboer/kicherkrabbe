package de.bennyboer.kicherkrabbe.assets.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortDirection;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetsSortProperty;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAsset;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.LookupAssetPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class InMemoryAssetLookupRepo extends InMemoryEventSourcingReadModelRepo<AssetId, LookupAsset>
        implements AssetLookupRepo {

    @Override
    public Mono<LookupAssetPage> find(
            Collection<AssetId> assetIds,
            Set<ContentType> contentTypes,
            AssetsSortProperty sortProperty,
            AssetsSortDirection sortDirection,
            long skip,
            long limit
    ) {
        Comparator<LookupAsset> comparator = switch (sortProperty) {
            case CREATED_AT -> Comparator.comparing(LookupAsset::getCreatedAt);
            case FILE_SIZE -> Comparator.comparing(LookupAsset::getFileSize);
        };

        if (sortDirection == AssetsSortDirection.DESCENDING) {
            comparator = comparator.reversed();
        }

        return getAll()
                .filter(asset -> assetIds.contains(asset.getId()))
                .filter(asset -> contentTypes.isEmpty() || contentTypes.contains(asset.getContentType()))
                .sort(comparator)
                .collectList()
                .flatMap(assets -> {
                    long total = assets.size();

                    return Flux.fromIterable(assets)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupAssetPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Flux<ContentType> findUniqueContentTypes(Collection<AssetId> assetIds) {
        return getAll()
                .filter(asset -> assetIds.contains(asset.getId()))
                .map(LookupAsset::getContentType)
                .distinct();
    }

}
