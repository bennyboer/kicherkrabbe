package de.bennyboer.kicherkrabbe.assets.persistence.lookup;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

public interface AssetLookupRepo extends EventSourcingReadModelRepo<AssetId, LookupAsset> {

    Mono<LookupAssetPage> find(
            Collection<AssetId> assetIds,
            Set<ContentType> contentTypes,
            AssetsSortProperty sortProperty,
            AssetsSortDirection sortDirection,
            long skip,
            long limit
    );

    Flux<ContentType> findUniqueContentTypes(Collection<AssetId> assetIds);

}
