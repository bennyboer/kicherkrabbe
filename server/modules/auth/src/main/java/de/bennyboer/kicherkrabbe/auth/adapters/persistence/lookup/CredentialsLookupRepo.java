package de.bennyboer.kicherkrabbe.auth.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.auth.internal.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.auth.internal.credentials.Name;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

public interface CredentialsLookupRepo extends EventSourcingReadModelRepo<CredentialsId, CredentialsLookup> {

    Mono<CredentialsId> findCredentialsIdByName(Name name);

}
