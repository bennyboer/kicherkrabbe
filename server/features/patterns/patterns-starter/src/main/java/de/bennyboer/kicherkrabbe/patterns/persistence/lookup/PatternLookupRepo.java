package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface PatternLookupRepo extends EventSourcingReadModelRepo<PatternId, LookupPattern> {

    Mono<LookupPatternPage> find(Collection<PatternId> patternIds, String searchTerm, long skip, long limit);

}
