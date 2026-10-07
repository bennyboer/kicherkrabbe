package de.bennyboer.kicherkrabbe.embroideries.persistence.categories;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface EmbroideryCategoryRepo {

    Mono<EmbroideryCategory> save(EmbroideryCategory category);

    Mono<Void> removeById(EmbroideryCategoryId id);

    Flux<EmbroideryCategory> findByIds(Set<EmbroideryCategoryId> categories);

    Flux<EmbroideryCategory> findAll();

}
