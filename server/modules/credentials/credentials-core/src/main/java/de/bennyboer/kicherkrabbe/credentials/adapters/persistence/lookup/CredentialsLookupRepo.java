package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CredentialsLookupRepo extends EventSourcingReadModelRepo<CredentialsId, CredentialsLookup> {

    Mono<CredentialsId> findCredentialsIdByName(Name name);

    Flux<CredentialsId> findCredentialsIdByUserId(UserId userId);

}
