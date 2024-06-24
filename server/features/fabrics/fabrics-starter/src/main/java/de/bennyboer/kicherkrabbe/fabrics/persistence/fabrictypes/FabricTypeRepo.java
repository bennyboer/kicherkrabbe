package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface FabricTypeRepo {

    Mono<FabricType> save(FabricType fabricType);

    Mono<Void> removeById(FabricTypeId id);

    Flux<FabricType> findByIds(Collection<FabricTypeId> ids);

    Flux<FabricType> findAll();
    
}
