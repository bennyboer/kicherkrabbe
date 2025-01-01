package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.mailing.api.requests.ClearMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateRateLimitRequest;
import de.bennyboer.kicherkrabbe.mailing.api.responses.*;
import de.bennyboer.kicherkrabbe.mailing.external.LoggingMailApi;
import de.bennyboer.kicherkrabbe.mailing.mail.MailService;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.inmemory.InMemoryMailLookupRepo;
import de.bennyboer.kicherkrabbe.mailing.settings.SettingsService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MailingModuleTest {

    private final TestClock clock = new TestClock();

    private final MailingModuleConfig config = new MailingModuleConfig();

    private final MailService mailService = new MailService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final SettingsService settingsService = new SettingsService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final MailLookupRepo mailLookupRepo = new InMemoryMailLookupRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    protected final LoggingMailApi mailApi = new LoggingMailApi();

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final MailingModule module = config.mailingModule(
            settingsService,
            mailService,
            mailLookupRepo,
            permissionsService,
            mailApi,
            transactionManager,
            Optional.of(clock)
    );

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public QueryMailsResponse getMails(long skip, long limit, Agent agent) {
        return module.getMails(skip, limit, agent).block();
    }

    public QueryMailResponse getMail(String mailId, Agent agent) {
        return module.getMail(mailId, agent).block();
    }

    public SendMailResponse sendMail(SendMailRequest request, Agent agent) {
        var result = module.sendMail(request, agent).block();

        updateMailInLookup(result.id);
        allowSystemUserToDeleteMail(result.id);
        allowUsersThatAreAllowedToReadMailsToReadMail(result.id);

        return result;
    }

    public void sendMailViaMailingService(String mailId, Agent agent) {
        module.sendMailViaMailingService(mailId, agent).block();
    }

    public QuerySettingsResponse getSettings(Agent agent) {
        return module.getSettings(agent).block();
    }

    public UpdateRateLimitResponse updateRateLimit(UpdateRateLimitRequest request, Agent agent) {
        return module.updateRateLimit(request, agent).block();
    }

    public UpdateMailgunApiTokenResponse updateMailgunApiToken(
            UpdateMailgunApiTokenRequest request,
            Agent agent
    ) {
        return module.updateMailgunApiToken(request, agent).block();
    }

    public ClearMailgunApiTokenResponse clearMailgunApiToken(
            ClearMailgunApiTokenRequest request,
            Agent agent
    ) {
        return module.clearMailgunApiToken(request, agent).block();
    }

    public List<String> cleanupOldMails(Agent agent) {
        var result = module.cleanupOldMails(agent).collectList().block();

        for (String mailId : result) {
            removeMailFromLookup(mailId);
            removePermissionsForMail(mailId);
        }

        return result;
    }

    public void allowSystemUserToSendMails() {
        module.allowSystemUserToSendMails().block();
    }

    public void allowUserToReadAndManageSettings(String userId) {
        module.allowUserToReadAndManageSettings(userId).block();
    }

    private void allowSystemUserToDeleteMail(String mailId) {
        module.allowSystemUserToDeleteMail(mailId).block();
    }

    private void allowUsersThatAreAllowedToReadMailsToReadMail(String mailId) {
        module.allowUsersThatAreAllowedToReadMailsToReadMail(mailId).block();
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
