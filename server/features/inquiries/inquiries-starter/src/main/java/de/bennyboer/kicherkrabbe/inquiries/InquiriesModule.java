package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.UpdateRateLimitsRequest;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.inquiries.api.responses.QueryStatusResponse;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.InquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.LookupInquiry;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.Request;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.RequestRepo;
import de.bennyboer.kicherkrabbe.inquiries.settings.*;
import de.bennyboer.kicherkrabbe.inquiries.transformers.LookupInquiryTransformer;
import de.bennyboer.kicherkrabbe.inquiries.transformers.RateLimitsTransformer;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static de.bennyboer.kicherkrabbe.inquiries.Actions.*;

public class InquiriesModule {

    private static final SettingsId DEFAULT_SETTINGS_ID = SettingsId.of("DEFAULT");

    private final InquiryService inquiryService;

    private final SettingsService settingsService;

    private final InquiryLookupRepo inquiryLookupRepo;

    private final RequestRepo requestRepo;

    private final PermissionsService permissionsService;

    private final Clock clock;

    public InquiriesModule(
            InquiryService inquiryService,
            SettingsService settingsService,
            InquiryLookupRepo inquiryLookupRepo,
            RequestRepo requestRepo,
            PermissionsService permissionsService,
            Clock clock
    ) {
        this.inquiryService = inquiryService;
        this.settingsService = settingsService;
        this.inquiryLookupRepo = inquiryLookupRepo;
        this.requestRepo = requestRepo;
        this.permissionsService = permissionsService;
        this.clock = clock;
    }

    private boolean isInitialized = false;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        initialize()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return allowAnonymousUserToQueryStatusAndSendInquiries();
    }

    public Mono<QueryStatusResponse> getStatus(Agent agent) {
        return assertAgentIsAllowedTo(agent, QUERY_STATUS)
                .then(getSettings())
                .map(Settings::isEnabled)
                .map(enabled -> {
                    var response = new QueryStatusResponse();
                    response.enabled = enabled;
                    return response;
                });
    }

    public Mono<QuerySettingsResponse> getSettings(Agent agent) {
        return assertAgentIsAllowedTo(agent, QUERY_SETTINGS)
                .then(getSettings())
                .map(settings -> {
                    var response = new QuerySettingsResponse();
                    response.enabled = settings.isEnabled();
                    response.rateLimits = RateLimitsTransformer.toApi(settings.getRateLimits());
                    return response;
                });
    }

    public Mono<Void> updateRateLimits(UpdateRateLimitsRequest request, Agent agent) {
        RateLimits rateLimits = RateLimitsTransformer.toInternal(request.rateLimits);

        return assertAgentIsAllowedTo(agent, UPDATE_RATE_LIMITS)
                .then(getSettings())
                .flatMap(settings -> settingsService.updateRateLimits(
                        settings.getId(),
                        settings.getVersion(),
                        rateLimits,
                        agent
                ))
                .then();
    }

    public Mono<String> sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent,
            @Nullable String ipAddress
    ) {
        var internalRequestId = RequestId.of(requestId);

        var senderName = SenderName.of(assertInputSafe(sender.name));
        var mail = EMail.of(assertInputSafe(sender.mail));
        var phone = Optional.ofNullable(sender.phone)
                .map(this::assertInputSafe)
                .map(PhoneNumber::of)
                .orElse(null);

        var internalSender = Sender.of(senderName, mail, phone);
        var internalSubject = Subject.of(assertInputSafe(subject));
        var internalMessage = Message.of(assertInputSafe(message));

        var fingerprint = Fingerprint.of(ipAddress);

        return assertAgentIsAllowedTo(agent, SEND)
                .then(assertRequestIdNotSeen(internalRequestId))
                .then(assertInquiriesEnabled())
                .then(assertNotRateLimited(mail, ipAddress))
                .then(inquiryService.send(
                        internalRequestId,
                        internalSender,
                        internalSubject,
                        internalMessage,
                        fingerprint,
                        agent
                ))
                .delayUntil(ignored -> requestRepo.insert(Request.of(
                        internalRequestId,
                        mail,
                        ipAddress,
                        clock.instant()
                )))
                .map(idAndVersion -> idAndVersion.getId().getValue());
    }

    public Mono<InquiryDTO> getInquiryByRequestId(String requestId, Agent agent) {
        return inquiryLookupRepo.findByRequestId(RequestId.of(requestId))
                .delayUntil(inquiry -> assertAgentIsAllowedTo(agent, READ, inquiry.getId()))
                .map(LookupInquiryTransformer::toApi);
    }

    public Mono<Void> setSendingInquiriesEnabled(boolean enabled, Agent agent) {
        return assertAgentIsAllowedTo(agent, ENABLE_OR_DISABLE_INQUIRIES)
                .then(getSettings())
                .flatMap(settings -> {
                    if (enabled) {
                        return settingsService.enable(settings.getId(), settings.getVersion(), agent);
                    } else {
                        return settingsService.disable(settings.getId(), settings.getVersion(), agent);
                    }
                })
                .then();
    }

    public Mono<Void> setMaximumInquiriesPerEmailPerTimeFrame(int count, Duration duration, Agent agent) {
        return assertAgentIsAllowedTo(agent, UPDATE_RATE_LIMITS)
                .then(getSettings())
                .flatMap(settings -> {
                    RateLimits rateLimits = settings.getRateLimits();

                    var updatedPerMailRateLimit = RateLimit.of(count, duration);
                    var updatedRateLimits = RateLimits.of(
                            updatedPerMailRateLimit,
                            rateLimits.getPerIp(),
                            rateLimits.getOverall()
                    );

                    return settingsService.updateRateLimits(
                            settings.getId(),
                            settings.getVersion(),
                            updatedRateLimits,
                            agent
                    );
                })
                .then();
    }

    public Mono<Void> setMaximumInquiriesPerIPAddressPerTimeFrame(int count, Duration duration, Agent agent) {
        return assertAgentIsAllowedTo(agent, UPDATE_RATE_LIMITS)
                .then(getSettings())
                .flatMap(settings -> {
                    RateLimits rateLimits = settings.getRateLimits();

                    var updatedPerIpRateLimit = RateLimit.of(count, duration);
                    var updatedRateLimits = RateLimits.of(
                            rateLimits.getPerMail(),
                            updatedPerIpRateLimit,
                            rateLimits.getOverall()
                    );

                    return settingsService.updateRateLimits(
                            settings.getId(),
                            settings.getVersion(),
                            updatedRateLimits,
                            agent
                    );
                })
                .then();
    }

    public Mono<Void> setMaximumInquiriesPerTimeFrame(int count, Duration duration, Agent agent) {
        return assertAgentIsAllowedTo(agent, UPDATE_RATE_LIMITS)
                .then(getSettings())
                .flatMap(settings -> {
                    RateLimits rateLimits = settings.getRateLimits();

                    var updatedOverallRateLimit = RateLimit.of(count, duration);
                    var updatedRateLimits = RateLimits.of(
                            rateLimits.getPerMail(),
                            rateLimits.getPerIp(),
                            updatedOverallRateLimit
                    );

                    return settingsService.updateRateLimits(
                            settings.getId(),
                            settings.getVersion(),
                            updatedRateLimits,
                            agent
                    );
                })
                .then();
    }

    public Mono<Void> updateInquiryInLookup(String inquiryId) {
        return inquiryService.getOrThrow(InquiryId.of(inquiryId))
                .flatMap(inquiry -> inquiryLookupRepo.update(LookupInquiry.of(
                        inquiry.getId(),
                        inquiry.getVersion(),
                        inquiry.getRequestId(),
                        inquiry.getSender(),
                        inquiry.getSubject(),
                        inquiry.getMessage(),
                        inquiry.getFingerprint(),
                        inquiry.getCreatedAt()
                )));
    }

    public Mono<Void> removeInquiryFromLookup(String inquiryId) {
        return inquiryLookupRepo.remove(InquiryId.of(inquiryId));
    }

    public Mono<Void> allowSystemToReadAndDeleteInquiry(String inquiryId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(inquiryId));
        var systemHolder = Holder.group(HolderId.system());

        var readInquiry = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(READ)
                .on(resource);
        var deleteInquiry = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermissions(readInquiry, deleteInquiry);
    }

    public Mono<Void> allowAnonymousUserToQueryStatusAndSendInquiries() {
        var anonymousHolder = Holder.group(HolderId.anonymous());

        var sendInquiriesPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(SEND)
                .onType(getResourceType());
        var queryStatusPermission = Permission.builder()
                .holder(anonymousHolder)
                .isAllowedTo(QUERY_STATUS)
                .onType(getResourceType());

        return permissionsService.addPermissions(sendInquiriesPermission, queryStatusPermission);
    }

    public Mono<Void> allowUserToManageInquiries(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        var querySettingsPermission = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(QUERY_SETTINGS)
                .onType(getResourceType());
        var enableOrDisableInquiriesPermissions = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(ENABLE_OR_DISABLE_INQUIRIES)
                .onType(getResourceType());
        var updateRateLimitsPermissions = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(UPDATE_RATE_LIMITS)
                .onType(getResourceType());

        return permissionsService.addPermissions(
                querySettingsPermission,
                enableOrDisableInquiriesPermissions,
                updateRateLimitsPermissions
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        return permissionsService.removePermissionsByHolder(userHolder);
    }

    public Mono<Void> removePermissions(String inquiryId) {
        var resource = Resource.of(getResourceType(), ResourceId.of(inquiryId));

        return permissionsService.removePermissionsByResource(resource);
    }

    private Mono<Void> assertRequestIdNotSeen(RequestId requestId) {
        return requestRepo.findById(requestId)
                .flatMap(request -> Mono.error(new TooManyRequestsException()));
    }

    private Mono<Void> assertNotRateLimited(EMail mail, @Nullable String ipAddress) {
        return getSettings()
                .map(Settings::getRateLimits)
                .delayUntil(rateLimits -> assertNotOverallRateLimited(rateLimits.getOverall()))
                .delayUntil(rateLimits -> assertNotRateLimitedByMail(mail, rateLimits.getPerMail()))
                .delayUntil(rateLimits -> Optional.ofNullable(ipAddress)
                        .map(ip -> assertNotRateLimitedByIpAddress(ip, rateLimits.getPerIp()))
                        .orElse(Mono.empty()))
                .then();
    }

    private Mono<Void> assertNotOverallRateLimited(RateLimit rateLimit) {
        Instant since = clock.instant().minus(rateLimit.getDuration());

        return requestRepo.countRecent(since)
                .flatMap(count -> {
                    if (count >= rateLimit.getMaxRequests()) {
                        return Mono.error(new TooManyRequestsException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> assertNotRateLimitedByMail(EMail mail, RateLimit rateLimit) {
        Instant since = clock.instant().minus(rateLimit.getDuration());

        return requestRepo.countRecentByMail(mail, since)
                .flatMap(count -> {
                    if (count >= rateLimit.getMaxRequests()) {
                        return Mono.error(new TooManyRequestsException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> assertNotRateLimitedByIpAddress(String ipAddress, RateLimit rateLimit) {
        Instant since = clock.instant().minus(rateLimit.getDuration());

        return requestRepo.countRecentByIpAddress(ipAddress, since)
                .flatMap(count -> {
                    if (count >= rateLimit.getMaxRequests()) {
                        return Mono.error(new TooManyRequestsException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> assertInquiriesEnabled() {
        return getSettings()
                .flatMap(settings -> {
                    if (settings.isDisabled()) {
                        return Mono.error(new InquiriesDisabledException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Settings> getSettings() {
        return settingsService.get(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(settingsService.init(DEFAULT_SETTINGS_ID, Agent.system())
                        .flatMap(idAndVersion -> settingsService.get(idAndVersion.getId())));
    }

    private String assertInputSafe(String input) {
        notNull(input, "Input must be given");

        var sanitized = Jsoup.clean(input, Safelist.basic());
        if (!input.equals(sanitized)) {
            throw new IllegalArgumentException("Input seems to be unsafe and is thus rejected");
        }

        return input;
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action) {
        return assertAgentIsAllowedTo(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedTo(Agent agent, Action action, @Nullable InquiryId inquiryId) {
        Permission permission = toPermission(agent, action, inquiryId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toPermission(Agent agent, Action action, @Nullable InquiryId inquiryId) {
        Holder holder = toHolder(agent);
        var resourceType = getResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(inquiryId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(inquiryId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Holder toHolder(Agent agent) {
        if (agent.isSystem()) {
            return Holder.group(HolderId.system());
        } else if (agent.isAnonymous()) {
            return Holder.group(HolderId.anonymous());
        } else {
            return Holder.user(HolderId.of(agent.getId().getValue()));
        }
    }

    private ResourceType getResourceType() {
        return ResourceType.of("INQUIRY");
    }

}
