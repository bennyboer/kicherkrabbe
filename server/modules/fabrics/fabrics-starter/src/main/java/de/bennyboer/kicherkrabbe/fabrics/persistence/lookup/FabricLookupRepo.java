package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrics.FabricId;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FabricLookupRepo extends EventSourcingReadModelRepo<FabricId, LookupFabric> {

    Mono<LookupFabricPage> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit);

}
