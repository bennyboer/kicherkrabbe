package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.FabricLookupRepo;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabric;
import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.LookupFabricPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class InMemoryFabricLookupRepo extends InMemoryEventSourcingReadModelRepo<FabricId, LookupFabric>
        implements FabricLookupRepo {

    @Override
    protected FabricId getId(LookupFabric readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<LookupFabricPage> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        return getAll()
                .filter(fabric -> fabricIds.contains(fabric.getId()))
                .filter(fabric -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return fabric.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupFabric::getCreatedAt))
                .collectList()
                .flatMap(fabrics -> {
                    long total = fabrics.size();

                    return Flux.fromIterable(fabrics)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupFabricPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupFabric> findPublished(FabricId id) {
        return getAll()
                .filter(fabric -> fabric.getId().equals(id) && fabric.isPublished())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupFabricPage> findPublished(
            String searchTerm,
            Set<ColorId> colors,
            Set<TopicId> topics,
            boolean filterAvailability,
            boolean inStock,
            boolean ascending,
            long skip,
            long limit
    ) {
        Comparator<LookupFabric> comparator = Comparator.comparing(fabric -> fabric.getName().getValue());
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return getAll()
                .filter(LookupFabric::isPublished)
                .filter(fabric -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return fabric.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .filter(fabric -> colors.isEmpty() || fabric.getColors().stream().anyMatch(colors::contains))
                .filter(fabric -> topics.isEmpty() || fabric.getTopics().stream().anyMatch(topics::contains))
                .filter(fabric -> !filterAvailability || fabric.getAvailability().stream().anyMatch(availability ->
                        availability.isInStock() == inStock
                ))
                .sort(comparator)
                .collectList()
                .flatMap(fabrics -> {
                    long total = fabrics.size();

                    return Flux.fromIterable(fabrics)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupFabricPage.of(skip, limit, total, results));
                });
    }

}
