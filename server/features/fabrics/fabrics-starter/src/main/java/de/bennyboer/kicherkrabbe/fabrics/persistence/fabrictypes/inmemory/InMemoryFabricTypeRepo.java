package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.inmemory;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFabricTypeRepo implements FabricTypeRepo {

    private final Map<FabricTypeId, FabricType> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<FabricType> save(FabricType fabricType) {
        return Mono.fromCallable(() -> {
            lookup.put(fabricType.getId(), fabricType);
            return fabricType;
        });
    }

    @Override
    public Mono<Void> removeById(FabricTypeId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Flux<FabricType> findByIds(Collection<FabricTypeId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

}
