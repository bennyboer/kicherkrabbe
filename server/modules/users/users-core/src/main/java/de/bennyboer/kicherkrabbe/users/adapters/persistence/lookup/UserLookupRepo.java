package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.users.UserId;
import reactor.core.publisher.Mono;

public interface UserLookupRepo extends EventSourcingReadModelRepo<UserId, UserLookup> {

    Mono<UserLookup> findByMail(Mail mail);

    Mono<Long> count();

}