package de.bennyboer.kicherkrabbe.eventsourcing.example.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

public class InMemoryEventSourcingReadModelTests extends EventSourcingReadModelTests {

    @Override
    protected SampleAggregateReadModelRepo createRepo() {
        return new InMemorySampleAggregateReadModelRepo();
    }

    public static class InMemorySampleAggregateReadModelRepo
            extends InMemoryEventSourcingReadModelRepo<String, SampleAggregateReadModel>
            implements SampleAggregateReadModelRepo {

        @Override
        protected String getId(SampleAggregateReadModel readModel) {
            return readModel.getId();
        }

        @Override
        public Mono<SampleAggregateReadModel> get(String id) {
            return super.get(id);
        }

    }

}
