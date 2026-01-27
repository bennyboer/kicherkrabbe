package de.bennyboer.kicherkrabbe.credentials.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.LookupCredentials;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.CredentialsLookupRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryCredentialsLookupRepo extends InMemoryEventSourcingReadModelRepo<CredentialsId, LookupCredentials>
        implements CredentialsLookupRepo {

    @Override
    public Mono<LookupCredentials> findCredentialsByName(Name name) {
        return getAll().filter(lookup -> lookup.getName().equals(name))
                .next();
    }

    @Override
    public Flux<LookupCredentials> findCredentialsByUserId(UserId userId) {
        return getAll().filter(lookup -> lookup.getUserId().equals(userId));
    }

    @Override
    protected CredentialsId getId(LookupCredentials readModel) {
        return readModel.getId();
    }

}
