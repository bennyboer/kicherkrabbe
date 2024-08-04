package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPatternPage;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class InMemoryPatternLookupRepo extends InMemoryEventSourcingReadModelRepo<PatternId, LookupPattern>
        implements PatternLookupRepo {

    @Override
    protected PatternId getId(LookupPattern readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<LookupPatternPage> find(
            Collection<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return getAll()
                .filter(pattern -> patternIds.contains(pattern.getId()))
                .filter(pattern -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return pattern.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupPattern::getCreatedAt))
                .collectList()
                .flatMap(patterns -> {
                    long total = patterns.size();

                    return Flux.fromIterable(patterns)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupPatternPage.of(skip, limit, total, results));
                });
    }

}
