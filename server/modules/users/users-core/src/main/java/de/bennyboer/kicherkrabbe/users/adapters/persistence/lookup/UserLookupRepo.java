package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import de.bennyboer.kicherkrabbe.users.internal.UserId;
import reactor.core.publisher.Mono;

public interface UserLookupRepo extends EventSourcingReadModelRepo<UserId, UserLookup> {

    Mono<UserLookup> findByMail(Mail mail);

}
