package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailId;
import de.bennyboer.kicherkrabbe.mailbox.mail.Status;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMail;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.LookupMailPage;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.MailLookupRepo;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Optional;

public class InMemoryMailLookupRepo
        extends InMemoryEventSourcingReadModelRepo<MailId, LookupMail>
        implements MailLookupRepo {

    @Override
    public Mono<LookupMail> findById(MailId id) {
        return get(id);
    }

    @Override
    public Mono<LookupMailPage> query(String searchTerm, @Nullable Status status, long skip, long limit) {
        return getAll()
                .filter(mail -> Optional.ofNullable(status)
                        .map(s -> mail.getStatus() == s)
                        .orElse(true))
                .filter(mail -> searchTerm.isBlank() || mail.getSubject()
                        .getValue()
                        .toLowerCase()
                        .contains(searchTerm.toLowerCase()) || mail.getSender()
                        .getName()
                        .getValue()
                        .toLowerCase()
                        .contains(searchTerm.toLowerCase()) || mail.getSender()
                        .getMail()
                        .getValue()
                        .toLowerCase()
                        .contains(searchTerm.toLowerCase()))
                .sort(Comparator.comparing(LookupMail::getReceivedAt).reversed())
                .collectList()
                .map(mails -> {
                    long total = mails.size();
                    long from = Math.min(skip, total);
                    long to = Math.min(skip + limit, total);

                    return LookupMailPage.of(total, mails.subList((int) from, (int) to));
                });
    }

    @Override
    protected MailId getId(LookupMail mail) {
        return mail.getId();
    }

}
