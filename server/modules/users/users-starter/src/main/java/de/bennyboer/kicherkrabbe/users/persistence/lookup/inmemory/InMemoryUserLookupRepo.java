package de.bennyboer.kicherkrabbe.users.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.Mail;
import de.bennyboer.kicherkrabbe.users.UserId;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.UserLookup;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.UserLookupRepo;
import reactor.core.publisher.Mono;

public class InMemoryUserLookupRepo extends InMemoryEventSourcingReadModelRepo<UserId, UserLookup>
        implements UserLookupRepo {

    @Override
    protected UserId getId(UserLookup readModel) {
        return readModel.getUserId();
    }

    @Override
    public Mono<UserLookup> findByMail(Mail mail) {
        return getAll()
                .filter(userLookup -> userLookup.getMail().equals(mail))
                .next();
    }

    @Override
    public Mono<Long> count() {
        return getAll().count();
    }

}
