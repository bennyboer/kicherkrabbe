package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.ColorId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ColorLookupRepo extends EventSourcingReadModelRepo<ColorId, LookupColor> {

    Mono<LookupColorPage> find(Collection<ColorId> colorIds, String searchTerm, long skip, long limit);

}
