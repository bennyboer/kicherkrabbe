package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailing.api.requests.ClearMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateRateLimitRequest;
import de.bennyboer.kicherkrabbe.mailing.api.responses.*;
import de.bennyboer.kicherkrabbe.mailing.external.MailApi;
import de.bennyboer.kicherkrabbe.mailing.mail.*;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.LookupMail;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.MailLookupRepo;
import de.bennyboer.kicherkrabbe.mailing.settings.*;
import de.bennyboer.kicherkrabbe.mailing.transformer.LookupMailTransformer;
import de.bennyboer.kicherkrabbe.mailing.transformer.ReceiverTransformer;
import de.bennyboer.kicherkrabbe.mailing.transformer.SenderTransformer;
import de.bennyboer.kicherkrabbe.mailing.transformer.SettingsTransformer;
import de.bennyboer.kicherkrabbe.permissions.*;
import jakarta.annotation.Nullable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.mailing.Actions.*;

public class MailingModule {

    private static final SettingsId DEFAULT_SETTINGS_ID = SettingsId.of("DEFAULT");

    private final SettingsService settingsService;

    private final MailService mailService;

    private final MailLookupRepo mailLookupRepo;

    private final PermissionsService permissionsService;

    private final MailApi mailApi;

    private final ReactiveTransactionManager transactionManager;

    private final Clock clock;

    private boolean isInitialized;

    public MailingModule(
            SettingsService settingsService,
            MailService mailService,
            MailLookupRepo mailLookupRepo,
            PermissionsService permissionsService,
            MailApi mailApi,
            ReactiveTransactionManager transactionManager,
            Clock clock
    ) {
        this.settingsService = settingsService;
        this.mailService = mailService;
        this.mailLookupRepo = mailLookupRepo;
        this.permissionsService = permissionsService;
        this.mailApi = mailApi;
        this.transactionManager = transactionManager;
        this.clock = clock;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent ignoredEvent) {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        var transactionalOperator = TransactionalOperator.create(transactionManager);

        initialize()
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public Mono<Void> initialize() {
        return allowSystemUserToSendMails();
    }

    public Mono<QueryMailsResponse> getMails(long skip, long limit, Agent agent) {
        return assertAgentIsAllowedOnMail(agent, READ)
                .then(mailLookupRepo.query(skip, limit))
                .map(page -> {
                    var response = new QueryMailsResponse();
                    response.total = page.getTotal();
                    response.mails = LookupMailTransformer.toApi(page.getMails());
                    return response;
                });
    }

    public Mono<QueryMailResponse> getMail(String mailId, Agent agent) {
        var internalMailId = MailId.of(mailId);

        return assertAgentIsAllowedOnMail(agent, READ, internalMailId)
                .then(mailLookupRepo.findById(internalMailId))
                .map(mail -> {
                    var response = new QueryMailResponse();
                    response.mail = LookupMailTransformer.toApi(mail);
                    return response;
                });
    }

    public Mono<SendMailResponse> sendMail(SendMailRequest request, Agent agent) {
        var sender = SenderTransformer.toInternal(request.sender);
        var receivers = ReceiverTransformer.toInternal(request.receivers);
        var subject = Subject.of(request.subject);
        var text = Text.of(request.text);

        return assertAgentIsAllowedOnMail(agent, SEND)
                .then(assertNotRateLimited())
                .then(mailService.send(sender, receivers, subject, text, MailingService.MAILGUN, agent))
                .map(idAndVersion -> {
                    var response = new SendMailResponse();
                    response.id = idAndVersion.getId().getValue();
                    response.version = idAndVersion.getVersion().getValue();
                    return response;
                });
    }

    public Mono<Void> sendMailViaMailingService(String mailId, Agent agent) {
        var internalMailId = MailId.of(mailId);

        return assertAgentIsAllowedOnMail(agent, SEND)
                .then(mailService.get(internalMailId))
                .flatMap(mail -> getSettings()
                        .flatMap(settings -> mailApi.sendMail(
                                mail.getSender(),
                                mail.getReceivers(),
                                mail.getSubject(),
                                mail.getText(),
                                settings
                        )))
                .then();
    }

    public Mono<QuerySettingsResponse> getSettings(Agent agent) {
        return assertAgentIsAllowedOnSettings(agent, READ)
                .then(getSettings())
                .map(settings -> {
                    var response = new QuerySettingsResponse();
                    response.settings = SettingsTransformer.toApi(settings);
                    return response;
                });
    }

    public Mono<UpdateRateLimitResponse> updateRateLimit(UpdateRateLimitRequest request, Agent agent) {
        var version = Version.of(request.version);
        var duration = Duration.ofMillis(request.durationInMs);
        var limit = request.limit;

        return assertAgentIsAllowedOnSettings(agent, UPDATE_RATE_LIMIT)
                .then(settingsService.updateRateLimit(DEFAULT_SETTINGS_ID, version, duration, limit, agent))
                .map(newVersion -> {
                    var response = new UpdateRateLimitResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<UpdateMailgunApiTokenResponse> updateMailgunApiToken(
            UpdateMailgunApiTokenRequest request,
            Agent agent
    ) {
        var version = Version.of(request.version);
        var apiToken = ApiToken.of(request.apiToken);

        return assertAgentIsAllowedOnSettings(agent, UPDATE_MAILGUN_API_TOKEN)
                .then(settingsService.updateMailgunApiToken(DEFAULT_SETTINGS_ID, version, apiToken, agent))
                .map(newVersion -> {
                    var response = new UpdateMailgunApiTokenResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<ClearMailgunApiTokenResponse> clearMailgunApiToken(
            ClearMailgunApiTokenRequest request,
            Agent agent
    ) {
        var version = Version.of(request.version);

        return assertAgentIsAllowedOnSettings(agent, CLEAR_MAILGUN_API_TOKEN)
                .then(settingsService.clearMailgunApiToken(DEFAULT_SETTINGS_ID, version, agent))
                .map(newVersion -> {
                    var response = new ClearMailgunApiTokenResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Flux<String> cleanupOldMails(Agent agent) {
        Instant ninetyDaysAgo = clock.instant().minus(90, ChronoUnit.DAYS);

        return mailLookupRepo.findOlderThan(ninetyDaysAgo)
                .delayUntil(notification -> deleteMail(notification.getId(), notification.getVersion(), agent))
                .map(notification -> notification.getId().getValue());
    }

    public Mono<Void> allowSystemUserToSendMails() {
        var systemHolder = Holder.group(HolderId.system());

        var sendMail = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(SEND)
                .onType(getMailResourceType());

        return permissionsService.addPermission(sendMail);
    }

    public Mono<Void> allowSystemUserToDeleteMail(String mailId) {
        var systemHolder = Holder.group(HolderId.system());
        var resource = Resource.of(getMailResourceType(), ResourceId.of(mailId));

        var deleteMail = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermission(deleteMail);
    }

    public Mono<Void> allowUserToReadAndManageSettings(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        var readMails = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getMailResourceType());
        var readSettings = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getSettingsResourceType());
        var updateRateLimit = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(UPDATE_RATE_LIMIT)
                .onType(getSettingsResourceType());
        var updateMailgunApiToken = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(UPDATE_MAILGUN_API_TOKEN)
                .onType(getSettingsResourceType());
        var clearMailgunApiToken = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(CLEAR_MAILGUN_API_TOKEN)
                .onType(getSettingsResourceType());

        return permissionsService.addPermissions(
                readMails,
                readSettings,
                updateRateLimit,
                updateMailgunApiToken,
                clearMailgunApiToken
        );
    }

    public Mono<Void> allowUsersThatAreAllowedToReadMailsToReadMail(String mailId) {
        var resource = Resource.of(getMailResourceType(), ResourceId.of(mailId));

        return permissionsService.findPermissionsByResourceTypeAndAction(getMailResourceType(), READ)
                .map(Permission::getHolder)
                .filter(holder -> holder.getType() == HolderType.USER)
                .collectList()
                .flatMapMany(holders -> {
                    var newPermissions = holders.stream()
                            .map(holder -> Permission.builder()
                                    .holder(holder)
                                    .isAllowedTo(READ)
                                    .on(resource))
                            .collect(Collectors.toSet());

                    return permissionsService.addPermissions(newPermissions);
                })
                .then();
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        return permissionsService.removePermissionsByHolder(Holder.user(HolderId.of(userId)));
    }

    public Mono<Void> updateMailInLookup(String mailId) {
        return mailService.get(MailId.of(mailId))
                .map(mail -> LookupMail.of(
                        mail.getId(),
                        mail.getVersion(),
                        mail.getSender(),
                        mail.getReceivers(),
                        mail.getSubject(),
                        mail.getText(),
                        mail.getSentAt()
                ))
                .flatMap(mailLookupRepo::update)
                .then();
    }

    public Mono<Void> removeMailFromLookup(String mailId) {
        return mailLookupRepo.remove(MailId.of(mailId));
    }

    public Mono<Void> removePermissionsForMail(String mailId) {
        var resource = Resource.of(getMailResourceType(), ResourceId.of(mailId));

        return permissionsService.removePermissionsByResource(resource);
    }

    private Mono<Void> assertNotRateLimited() {
        return getSettings()
                .flatMap(settings -> {
                    RateLimitSettings rateLimit = settings.getRateLimit();
                    Duration duration = rateLimit.getDuration();
                    long limit = rateLimit.getLimit();

                    return mailLookupRepo.countAfter(clock.instant().minus(duration))
                            .filter(count -> count < limit)
                            .switchIfEmpty(Mono.error(new RateLimitExceededError()));
                })
                .then();
    }

    private Mono<Settings> getSettings() {
        return settingsService.get(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(settingsService.init(DEFAULT_SETTINGS_ID, Agent.system())
                        .flatMap(idAndVersion -> settingsService.get(idAndVersion.getId())));
    }

    private Mono<Void> deleteMail(MailId mailId, Version version, Agent agent) {
        return assertAgentIsAllowedOnMail(agent, DELETE, mailId)
                .then(mailService.get(mailId)
                        .flatMap(mail -> mailService.delete(mail.getId(), version, Agent.system())))
                .then(mailLookupRepo.remove(mailId));
    }

    private Mono<Void> assertAgentIsAllowedOnMail(Agent agent, Action action) {
        return assertAgentIsAllowedOnMail(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedOnMail(Agent agent, Action action, @Nullable MailId mailId) {
        Permission permission = toMailPermission(agent, action, mailId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toMailPermission(Agent agent, Action action, @Nullable MailId mailId) {
        Holder holder = toHolder(agent);
        var resourceType = getMailResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(mailId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(mailId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
    }

    private Mono<Void> assertAgentIsAllowedOnSettings(Agent agent, Action action) {
        return assertAgentIsAllowedOnSettings(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedOnSettings(Agent agent, Action action, @Nullable SettingsId settingsId) {
        Permission permission = toSettingsPermission(agent, action, settingsId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toSettingsPermission(Agent agent, Action action, @Nullable SettingsId settingsId) {
        Holder holder = toHolder(agent);
        var resourceType = getSettingsResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(settingsId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(settingsId.getValue()))))
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

    private ResourceType getSettingsResourceType() {
        return ResourceType.of("MAILING_SETTINGS");
    }

    private ResourceType getMailResourceType() {
        return ResourceType.of("MAILING_MAIL");
    }

}
