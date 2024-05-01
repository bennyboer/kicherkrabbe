package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo;

import reactor.core.publisher.Mono;

public interface ReadModelSerializer<D, S> {

    Mono<S> serialize(D readModel);

    Mono<D> deserialize(S serialized);

}
