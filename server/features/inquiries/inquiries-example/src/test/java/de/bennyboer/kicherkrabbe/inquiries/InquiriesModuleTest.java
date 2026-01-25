package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.samples.SampleInquiry;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.UpdateRateLimitsRequest;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QueryRequestStatisticsResponse;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QueryStatusResponse;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.InquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.inmemory.InMemoryInquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.RequestRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.inmemory.InMemoryRequestRepo;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsService;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class InquiriesModuleTest {

    protected final TestClock clock = new TestClock();

    private final InquiriesModuleConfig config = new InquiriesModuleConfig();

    private final InquiryService inquiryService = new InquiryService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final SettingsService settingsService = new SettingsService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            clock
    );

    private final InquiryLookupRepo inquiryLookupRepo = new InMemoryInquiryLookupRepo();

    private final RequestRepo requestRepo = new InMemoryRequestRepo();

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final InquiriesModule module = config.inquiriesModule(
            inquiryService,
            settingsService,
            inquiryLookupRepo,
            requestRepo,
            permissionsService,
            transactionManager,
            Optional.of(clock)
    );

    protected final String loggedInUserId = "USER_ID";

    public QueryStatusResponse getStatus(Agent agent) {
        return module.getStatus(agent).block();
    }

    public QuerySettingsResponse getSettings(Agent agent) {
        return module.getSettings(agent).block();
    }

    public QueryRequestStatisticsResponse getRequestStatistics(Instant from, Instant to, Agent agent) {
        return module.getRequestStatistics(from, to, agent).block();
    }

    public String sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent
    ) {
        return sendInquiry(requestId, sender, subject, message, agent, "127.0.0.1");
    }

    public String sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent,
            String ipAddress
    ) {
        var id = module.sendInquiry(
                requestId,
                sender,
                subject,
                message,
                agent,
                ipAddress
        ).block();

        updateInquiryInLookup(id);
        allowSystemToReadAndDeleteInquiry(id);

        return id;
    }

    public String sendInquiry(SampleInquiry sample, Agent agent) {
        return sendInquiry(
                sample.getRequestId(),
                sample.getSenderDTO(),
                sample.getSubject(),
                sample.getMessage(),
                agent
        );
    }

    public String sendSampleInquiry(Agent agent) {
        return sendInquiry(SampleInquiry.builder().build(), agent);
    }

    public void deleteInquiry(String inquiryId, Agent agent) {
        module.deleteInquiry(inquiryId, agent).block();

        module.removeInquiryFromLookup(inquiryId).block();
        module.removePermissions(inquiryId).block();
    }

    public InquiryDTO getInquiryByRequestId(String requestId, Agent agent) {
        return module.getInquiryByRequestId(requestId, agent).block();
    }

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public void disableSendingInquiries() {
        disableSendingInquiries(Agent.user(AgentId.of(loggedInUserId)));
    }

    public void disableSendingInquiries(Agent agent) {
        module.setSendingInquiriesEnabled(false, agent).block();
    }

    public void enableSendingInquiries() {
        enableSendingInquiries(Agent.user(AgentId.of(loggedInUserId)));
    }

    public void enableSendingInquiries(Agent agent) {
        module.setSendingInquiriesEnabled(true, agent).block();
    }

    public void updateRateLimits(UpdateRateLimitsRequest request, Agent agent) {
        module.updateRateLimits(request, agent).block();
    }

    public void allowAnonymousUserToQueryStatusAndSendInquiries() {
        module.allowAnonymousUserToQueryStatusAndSendInquiries().block();
    }

    public void allowUserToManageInquiries(String userId) {
        module.allowUserToManageInquiries(userId).block();
    }

    public void setMaximumInquiriesPerEmailPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerEmailPerTimeFrame(count, duration, Agent.user(AgentId.of(loggedInUserId))).block();
    }

    public void setMaximumInquiriesPerIPAddressPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerIPAddressPerTimeFrame(count, duration, Agent.user(AgentId.of(loggedInUserId)))
                .block();
    }

    public void setMaximumInquiriesPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerTimeFrame(count, duration, Agent.user(AgentId.of(loggedInUserId))).block();
    }

    private void updateInquiryInLookup(String inquiryId) {
        module.updateInquiryInLookup(inquiryId).block();
    }

    private void allowSystemToReadAndDeleteInquiry(String inquiryId) {
        module.allowSystemToReadAndDeleteInquiry(inquiryId).block();
    }

}
