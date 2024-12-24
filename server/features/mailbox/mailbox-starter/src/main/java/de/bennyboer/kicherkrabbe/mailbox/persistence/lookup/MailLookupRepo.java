package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailId;
import de.bennyboer.kicherkrabbe.mailbox.mail.Status;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

public interface MailLookupRepo extends EventSourcingReadModelRepo<MailId, LookupMail> {

    Mono<LookupMail> findById(MailId id);

    Mono<LookupMailPage> query(String searchTerm, @Nullable Status status, long skip, long limit);

    Mono<Long> countUnread();

}
