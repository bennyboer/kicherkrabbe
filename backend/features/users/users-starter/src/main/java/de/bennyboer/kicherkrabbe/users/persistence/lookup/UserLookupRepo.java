package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.commons.UserId;
import reactor.core.publisher.Mono;

public interface UserLookupRepo extends EventSourcingReadModelRepo<UserId, LookupUser> {

    Mono<LookupUser> findByMail(Mail mail);

    Mono<Long> count();

}
