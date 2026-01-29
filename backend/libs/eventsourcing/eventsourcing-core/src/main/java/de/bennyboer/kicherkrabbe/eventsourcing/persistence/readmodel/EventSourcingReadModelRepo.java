package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel;

import reactor.core.publisher.Mono;

public interface EventSourcingReadModelRepo<ID, T extends VersionedReadModel<ID>> {

    Mono<Void> update(T readModel);

    Mono<Void> remove(ID id);

}
