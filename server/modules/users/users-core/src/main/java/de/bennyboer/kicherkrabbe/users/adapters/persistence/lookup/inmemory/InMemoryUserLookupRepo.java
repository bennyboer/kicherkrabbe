package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookup;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import de.bennyboer.kicherkrabbe.users.internal.UserId;
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
