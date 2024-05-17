package de.bennyboer.kicherkrabbe.fabrics.persistence.colors;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ColorRepo {

    Mono<Color> save(Color topic);

    Mono<Void> removeById(ColorId id);

    Flux<Color> findByIds(Collection<ColorId> ids);

}
