package de.bennyboer.kicherkrabbe.fabrics.persistence.colors.inmemory;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryColorRepo implements ColorRepo {

    private final Map<ColorId, Color> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<Color> save(Color color) {
        return Mono.fromCallable(() -> {
            lookup.put(color.getId(), color);
            return color;
        });
    }

    @Override
    public Mono<Void> removeById(ColorId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Flux<Color> findByIds(Collection<ColorId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

}
