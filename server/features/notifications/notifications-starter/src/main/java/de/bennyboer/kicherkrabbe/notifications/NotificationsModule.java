package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.DeactivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.responses.*;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import de.bennyboer.kicherkrabbe.notifications.notification.*;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.LookupNotification;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.NotificationLookupRepo;
import de.bennyboer.kicherkrabbe.notifications.settings.ActivatableChannel;
import de.bennyboer.kicherkrabbe.notifications.settings.Settings;
import de.bennyboer.kicherkrabbe.notifications.settings.SettingsId;
import de.bennyboer.kicherkrabbe.notifications.settings.SettingsService;
import de.bennyboer.kicherkrabbe.notifications.transformer.*;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.notifications.Actions.*;

public class NotificationsModule {

    private static final SettingsId DEFAULT_SETTINGS_ID = SettingsId.of("DEFAULT");

    private final NotificationService notificationService;

    private final SettingsService settingsService;

    private final NotificationLookupRepo notificationLookupRepo;

    private final PermissionsService permissionsService;

    private final ReactiveTransactionManager transactionManager;

    private final Clock clock;

    private boolean isInitialized = false;

    public NotificationsModule(
            NotificationService notificationService,
            SettingsService settingsService,
            NotificationLookupRepo notificationLookupRepo,
            PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager,
            Clock clock
    ) {
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.notificationLookupRepo = notificationLookupRepo;
        this.permissionsService = permissionsService;
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
        return allowSystemUserToSendNotifications();
    }

    public Mono<SendNotificationResponse> sendNotification(SendNotificationRequest request, Agent agent) {
        Origin origin = OriginTransformer.toInternal(request.origin);
        Target target = TargetTransformer.toInternal(request.target);
        var title = Title.of(request.title);
        var message = Message.of(request.message);

        return assertAgentIsAllowedOnNotifications(agent, SEND)
                .then(resolveChannels(target))
                .flatMap(channels -> notificationService.send(origin, target, channels, title, message, agent))
                .map(idAndVersion -> {
                    var response = new SendNotificationResponse();
                    response.id = idAndVersion.getId().getValue();
                    response.version = idAndVersion.getVersion().getValue();
                    return response;
                });
    }

    public Mono<QueryNotificationsResponse> getNotifications(
            DateRangeFilter dateRangeFilter,
            long skip,
            long limit,
            Agent agent
    ) {
        return assertAgentIsAllowedOnNotifications(agent, READ)
                .then(notificationLookupRepo.query(dateRangeFilter, skip, limit))
                .map(page -> {
                    var response = new QueryNotificationsResponse();
                    response.total = page.getTotal();
                    response.notifications = LookupNotificationTransformer.toApi(page.getNotifications());
                    return response;
                });
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

    public Mono<EnableSystemNotificationsResponse> enableSystemNotifications(long version, Agent agent) {
        return assertAgentIsAllowedOnSettings(agent, ENABLE_SYSTEM_NOTIFICATIONS)
                .then(settingsService.enableSystemNotifications(DEFAULT_SETTINGS_ID, Version.of(version), agent))
                .map(newVersion -> {
                    var response = new EnableSystemNotificationsResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<DisableSystemNotificationsResponse> disableSystemNotifications(long version, Agent agent) {
        return assertAgentIsAllowedOnSettings(agent, DISABLE_SYSTEM_NOTIFICATIONS)
                .then(settingsService.disableSystemNotifications(DEFAULT_SETTINGS_ID, Version.of(version), agent))
                .map(newVersion -> {
                    var response = new DisableSystemNotificationsResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<UpdateSystemChannelResponse> updateSystemChannel(UpdateSystemChannelRequest request, Agent agent) {
        var version = Version.of(request.version);
        Channel channel = ChannelTransformer.toInternal(request.channel);

        return assertAgentIsAllowedOnSettings(agent, UPDATE_SYSTEM_CHANNEL)
                .then(settingsService.updateSystemChannel(DEFAULT_SETTINGS_ID, version, channel, agent))
                .map(newVersion -> {
                    var response = new UpdateSystemChannelResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<ActivateSystemChannelResponse> activateSystemChannel(
            ActivateSystemChannelRequest request,
            Agent agent
    ) {
        var version = Version.of(request.version);
        ChannelType channelType = ChannelTypeTransformer.toInternal(request.channelType);

        return assertAgentIsAllowedOnSettings(agent, ACTIVATE_SYSTEM_CHANNEL)
                .then(settingsService.activateSystemChannel(DEFAULT_SETTINGS_ID, version, channelType, agent))
                .map(newVersion -> {
                    var response = new ActivateSystemChannelResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<DeactivateSystemChannelResponse> deactivateSystemChannel(
            DeactivateSystemChannelRequest request,
            Agent agent
    ) {
        var version = Version.of(request.version);
        ChannelType channelType = ChannelTypeTransformer.toInternal(request.channelType);

        return assertAgentIsAllowedOnSettings(agent, DEACTIVATE_SYSTEM_CHANNEL)
                .then(settingsService.deactivateSystemChannel(DEFAULT_SETTINGS_ID, version, channelType, agent))
                .map(newVersion -> {
                    var response = new DeactivateSystemChannelResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Flux<String> cleanupOldNotifications(Agent agent) {
        Instant ninetyDaysAgo = clock.instant().minus(90, ChronoUnit.DAYS);

        return notificationLookupRepo.findOlderThan(ninetyDaysAgo)
                .delayUntil(notification -> deleteNotification(notification.getId(), notification.getVersion(), agent))
                .map(notification -> notification.getId().getValue());
    }

    public Mono<Void> allowSystemUserToSendNotifications() {
        var systemHolder = Holder.group(HolderId.system());

        var sendNotifications = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(SEND)
                .onType(getNotificationResourceType());

        return permissionsService.addPermission(sendNotifications);
    }

    public Mono<Void> allowSystemUserToDeleteNotification(String notificationId) {
        var systemHolder = Holder.group(HolderId.system());
        var resource = Resource.of(getNotificationResourceType(), ResourceId.of(notificationId));

        var deleteNotification = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(DELETE)
                .on(resource);

        return permissionsService.addPermission(deleteNotification);
    }

    public Mono<Void> allowUserToReadNotificationsAndManageSettings(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        var readNotifications = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getNotificationResourceType());
        var readSettings = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getSettingsResourceType());
        var enableSystemNotifications = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(ENABLE_SYSTEM_NOTIFICATIONS)
                .onType(getSettingsResourceType());
        var disableSystemNotifications = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(DISABLE_SYSTEM_NOTIFICATIONS)
                .onType(getSettingsResourceType());
        var updateSystemChannel = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(UPDATE_SYSTEM_CHANNEL)
                .onType(getSettingsResourceType());
        var activateSystemChannel = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(ACTIVATE_SYSTEM_CHANNEL)
                .onType(getSettingsResourceType());
        var deactivateSystemChannel = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(DEACTIVATE_SYSTEM_CHANNEL)
                .onType(getSettingsResourceType());

        return permissionsService.addPermissions(
                readNotifications,
                readSettings,
                enableSystemNotifications,
                disableSystemNotifications,
                updateSystemChannel,
                activateSystemChannel,
                deactivateSystemChannel
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        return permissionsService.removePermissionsByHolder(Holder.user(HolderId.of(userId)));
    }

    public Mono<Void> updateNotificationInLookup(String notificationId) {
        return notificationService.get(NotificationId.of(notificationId))
                .map(notification -> LookupNotification.of(
                        notification.getId(),
                        notification.getVersion(),
                        notification.getOrigin(),
                        notification.getTarget(),
                        notification.getChannels(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getSentAt()
                ))
                .flatMap(notificationLookupRepo::update);
    }

    public Mono<Void> removeNotificationFromLookup(String notificationId) {
        return notificationLookupRepo.remove(NotificationId.of(notificationId));
    }

    public Mono<Void> removePermissionsForNotification(String notificationId) {
        var resource = Resource.of(getNotificationResourceType(), ResourceId.of(notificationId));

        return permissionsService.removePermissionsByResource(resource);
    }

    private Mono<Set<Channel>> resolveChannels(Target target) {
        return switch (target.getType()) {
            case SYSTEM -> resolveSystemChannels();
        };
    }

    private Mono<Set<Channel>> resolveSystemChannels() {
        return getSettings()
                .flatMapMany(settings -> Flux.fromIterable(settings.getSystemSettings().getChannels()))
                .filter(ActivatableChannel::isActive)
                .map(ActivatableChannel::getChannel)
                .collect(Collectors.toSet());
    }

    private Mono<Settings> getSettings() {
        return settingsService.get(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(settingsService.init(DEFAULT_SETTINGS_ID, Agent.system())
                        .flatMap(idAndVersion -> settingsService.get(idAndVersion.getId())));
    }

    private Mono<Void> deleteNotification(NotificationId notificationId, Version version, Agent agent) {
        return assertAgentIsAllowedOnNotification(agent, DELETE, notificationId)
                .then(notificationService.delete(notificationId, version, Agent.system()))
                .then();
    }

    private Mono<Void> assertAgentIsAllowedOnSettings(Agent agent, Action action) {
        return assertAgentIsAllowedOnSettings(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedOnSettings(Agent agent, Action action, @Nullable SettingsId settingsId) {
        Permission permission = toSettingsPermission(agent, action, settingsId);
        return permissionsService.assertHasPermission(permission);
    }

    private Mono<Void> assertAgentIsAllowedOnNotifications(Agent agent, Action action) {
        return assertAgentIsAllowedOnNotification(agent, action, null);
    }

    private Mono<Void> assertAgentIsAllowedOnNotification(
            Agent agent,
            Action action,
            @Nullable NotificationId notificationId
    ) {
        Permission permission = toNotificationPermission(agent, action, notificationId);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toNotificationPermission(Agent agent, Action action, @Nullable NotificationId notificationId) {
        Holder holder = toHolder(agent);
        var resourceType = getNotificationResourceType();

        Permission.Builder permissionBuilder = Permission.builder()
                .holder(holder)
                .isAllowedTo(action);

        return Optional.ofNullable(notificationId)
                .map(id -> permissionBuilder.on(Resource.of(resourceType, ResourceId.of(notificationId.getValue()))))
                .orElseGet(() -> permissionBuilder.onType(resourceType));
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

    private ResourceType getNotificationResourceType() {
        return ResourceType.of("NOTIFICATION");
    }

    private ResourceType getSettingsResourceType() {
        return ResourceType.of("NOTIFICATION_SETTINGS");
    }

}
