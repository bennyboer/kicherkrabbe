package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternAlias;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.PatternNumber;
import reactor.core.publisher.Flux;
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

    Flux<LookupPattern> findByCategory(PatternCategoryId categoryId);

    Mono<LookupPattern> findById(PatternId internalPatternId);

    Mono<LookupPattern> findByAlias(PatternAlias alias);

    Flux<PatternCategoryId> findUniqueCategories();

    Mono<LookupPattern> findPublished(PatternId id);

    Mono<LookupPatternPage> findPublished(
            String searchTerm,
            Set<PatternCategoryId> categories,
            Set<Long> sizes,
            boolean ascending,
            long skip,
            long limit
    );

    Mono<LookupPattern> findByNumber(PatternNumber number);

}
