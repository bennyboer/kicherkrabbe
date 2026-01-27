package de.bennyboer.kicherkrabbe.patterns.persistence.categories;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

public interface PatternCategoryRepo {

    Mono<PatternCategory> save(PatternCategory category);

    Mono<Void> removeById(PatternCategoryId id);

    Flux<PatternCategory> findByIds(Set<PatternCategoryId> categories);

    Flux<PatternCategory> findAll();

}
