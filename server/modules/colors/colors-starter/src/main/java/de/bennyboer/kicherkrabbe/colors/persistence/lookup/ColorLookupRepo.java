package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Flux;

public interface ColorLookupRepo extends EventSourcingReadModelRepo<ColorId, LookupColor> {

    Flux<LookupColor> find(String searchTerm, long skip, long limit);

}
