package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.FabricTypeLookupRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricType;
import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.LookupFabricTypePage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class InMemoryFabricTypeLookupRepo extends InMemoryEventSourcingReadModelRepo<FabricTypeId, LookupFabricType>
        implements FabricTypeLookupRepo {

    @Override
    protected FabricTypeId getId(LookupFabricType readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<LookupFabricTypePage> find(
            Collection<FabricTypeId> fabricTypeIds,
            String searchTerm,
            long skip,
            long limit
    ) {
        return getAll()
                .filter(fabric -> fabricTypeIds.contains(fabric.getId()))
                .filter(fabric -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return fabric.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupFabricType::getCreatedAt))
                .collectList()
                .flatMap(fabrics -> {
                    long total = fabrics.size();

                    return Flux.fromIterable(fabrics)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupFabricTypePage.of(skip, limit, total, results));
                });
    }

}
