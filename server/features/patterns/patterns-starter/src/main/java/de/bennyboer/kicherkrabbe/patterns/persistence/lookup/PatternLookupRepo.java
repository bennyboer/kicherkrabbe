package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

public interface PatternLookupRepo extends EventSourcingReadModelRepo<PatternId, LookupPattern> {

    Mono<LookupPatternPage> find(
            Collection<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    );

}
