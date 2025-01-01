package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailing.mail.MailId;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMail;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMailPage;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.MailLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;

public class InMemoryMailLookupRepo
        extends InMemoryEventSourcingReadModelRepo<MailId, LookupMail>
        implements MailLookupRepo {

    @Override
    public Mono<LookupMail> findById(MailId id) {
        return get(id);
    }

    @Override
    public Mono<Long> countAfter(Instant instant) {
        return getAll()
                .filter(mail -> mail.getSentAt().isAfter(instant))
                .count();
    }

    @Override
    public Mono<LookupMailPage> query(long skip, long limit) {
        return getAll()
                .sort(Comparator.comparing(LookupMail::getSentAt).reversed())
                .collectList()
                .map(mails -> {
                    long total = mails.size();
                    long from = Math.min(skip, total);
                    long to = Math.min(skip + limit, total);

                    return LookupMailPage.of(total, mails.subList((int) from, (int) to));
                });
    }

    @Override
    public Flux<LookupMail> findOlderThan(Instant instant) {
        return getAll()
                .filter(mail -> mail.getSentAt().isBefore(instant));
    }

    @Override
    protected MailId getId(LookupMail mail) {
        return mail.getId();
    }

}
