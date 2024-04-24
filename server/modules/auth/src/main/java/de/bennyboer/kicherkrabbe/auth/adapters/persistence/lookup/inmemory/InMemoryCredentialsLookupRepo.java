package de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
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
    protected CredentialsId getId(CredentialsLookup readModel) {
        return readModel.getId();
    }

}
