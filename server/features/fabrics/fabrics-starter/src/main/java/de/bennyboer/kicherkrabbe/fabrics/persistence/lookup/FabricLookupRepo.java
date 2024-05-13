package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

public interface FabricLookupRepo extends EventSourcingReadModelRepo<FabricId, LookupFabric> {

    Mono<LookupFabricPage> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit);

    Mono<LookupFabric> findPublished(FabricId id);

    Mono<LookupFabricPage> findPublished(
            String searchTerm,
            Set<ColorId> colors,
            Set<TopicId> topics,
            boolean filterAvailability,
            boolean inStock,
            boolean ascending,
            long skip,
            long limit
    );

    Flux<LookupFabric> findByColor(ColorId colorId);

    Flux<LookupFabric> findByTopic(TopicId topicId);

    Flux<LookupFabric> findByFabricType(FabricTypeId fabricTypeId);

    Flux<ColorId> findUniqueColors();

    Flux<TopicId> findUniqueTopics();
    
}
