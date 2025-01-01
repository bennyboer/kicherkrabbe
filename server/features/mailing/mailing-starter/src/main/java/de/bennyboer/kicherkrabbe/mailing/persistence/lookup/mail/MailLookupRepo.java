package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailing.mail.MailId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface MailLookupRepo extends EventSourcingReadModelRepo<MailId, LookupMail> {

    Mono<LookupMail> findById(MailId id);

    Mono<Long> countAfter(Instant instant);

    Mono<LookupMailPage> query(long skip, long limit);

    Flux<LookupMail> findOlderThan(Instant instant);

}
