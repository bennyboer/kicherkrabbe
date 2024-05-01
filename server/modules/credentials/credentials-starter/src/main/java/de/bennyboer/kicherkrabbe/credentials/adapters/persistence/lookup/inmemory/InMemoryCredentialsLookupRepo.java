package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryCredentialsLookupRepo extends InMemoryEventSourcingReadModelRepo<CredentialsId, CredentialsLookup>
        implements CredentialsLookupRepo {

    @Override
    public Mono<CredentialsId> findCredentialsIdByName(Name name) {
        return getAll().filter(lookup -> lookup.getName().equals(name))
                .map(CredentialsLookup::getId)
                .next();
    }

    @Override
    public Flux<CredentialsId> findCredentialsIdByUserId(UserId userId) {
        return getAll().filter(lookup -> lookup.getUserId().equals(userId))
                .map(CredentialsLookup::getId);
    }

    @Override
    protected CredentialsId getId(CredentialsLookup readModel) {
        return readModel.getId();
    }

}
