package de.bennyboer.kicherkrabbe.eventsourcing.example.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

public interface SampleAggregateReadModelRepo extends EventSourcingReadModelRepo<String, SampleAggregateReadModel> {

    Mono<SampleAggregateReadModel> get(String id);

}
