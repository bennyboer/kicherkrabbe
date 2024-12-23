package de.bennyboer.kicherkrabbe.mailbox;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailbox.api.StatusDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsReadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.MarkMailAsUnreadRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import de.bennyboer.kicherkrabbe.mailbox.api.responses.*;
import de.bennyboer.kicherkrabbe.mailbox.mail.MailService;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.inmemory.InMemoryMailLookupRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class MailboxModuleTest {

    private final TestClock clock = new TestClock();

    private final MailboxModuleConfig config = new MailboxModuleConfig();

    private final MailService mailService = new MailService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final MailLookupRepo mailLookupRepo = new InMemoryMailLookupRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final MailboxModule module = config.mailboxModule(mailService, mailLookupRepo, permissionsService);

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public ReceiveMailResponse receiveMail(ReceiveMailRequest request, Agent agent) {
        var result = module.receiveMail(request, agent).block();

        updateMailInLookup(result.mailId);
        allowUsersThatAreAllowedToManageMailsToManageMail(result.mailId);

        return result;
    }

    public QueryMailResponse getMail(String mailId, Agent agent) {
        return module.getMail(mailId, agent).block();
    }

    public QueryMailsResponse getMails(
            String searchTerm,
            @Nullable StatusDTO status,
            long skip,
            long limit,
            Agent agent
    ) {
        return module.getMails(searchTerm, status, skip, limit, agent).block();
    }

    public MarkMailAsReadResponse markMailAsRead(String mailId, MarkMailAsReadRequest request, Agent agent) {
        var result = module.markMailAsRead(mailId, request, agent).block();

        updateMailInLookup(mailId);

        return result;
    }

    public MarkMailAsUnreadResponse markMailAsUnread(String mailId, MarkMailAsUnreadRequest request, Agent agent) {
        var result = module.markMailAsUnread(mailId, request, agent).block();

        updateMailInLookup(mailId);

        return result;
    }

    public DeleteMailResponse deleteMail(String mailId, long version, Agent agent) {
        var result = module.deleteMail(mailId, version, agent).block();

        removeMailFromLookup(mailId);
        removePermissionsForMail(mailId);

        return result;
    }

    public void allowSystemUserToReceiveMails() {
        module.allowSystemUserToReceiveMails().block();
    }

    public void allowUserToReadAndManageMails(String userId) {
        module.allowUserToReadAndManageMails(userId).block();
    }

    private void allowUsersThatAreAllowedToManageMailsToManageMail(String mailId) {
        module.allowUsersThatAreAllowedToManageMailsToManageMail(mailId).block();
    }

    private void updateMailInLookup(String mailId) {
        module.updateMailInLookup(mailId).block();
    }

    private void removeMailFromLookup(String mailId) {
        module.removeMailFromLookup(mailId).block();
    }

    private void removePermissionsForMail(String mailId) {
        module.removePermissionsForMail(mailId).block();
    }

}
