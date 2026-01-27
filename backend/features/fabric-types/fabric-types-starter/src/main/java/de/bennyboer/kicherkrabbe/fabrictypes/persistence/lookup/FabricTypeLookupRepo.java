package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.fabrictypes.FabricTypeId;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FabricTypeLookupRepo extends EventSourcingReadModelRepo<FabricTypeId, LookupFabricType> {

    Mono<LookupFabricTypePage> find(Collection<FabricTypeId> fabricTypeIds, String searchTerm, long skip, long limit);

}
