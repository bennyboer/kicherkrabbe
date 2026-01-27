package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.telegram.api.requests.ClearBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.responses.ClearBotApiTokenResponse;
import de.bennyboer.kicherkrabbe.telegram.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.telegram.api.responses.UpdateBotApiTokenResponse;
import de.bennyboer.kicherkrabbe.telegram.external.TelegramApi;
import de.bennyboer.kicherkrabbe.telegram.settings.*;
import de.bennyboer.kicherkrabbe.telegram.transformer.SettingsTransformer;
import jakarta.annotation.Nullable;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

import static de.bennyboer.kicherkrabbe.telegram.Actions.*;

public class TelegramModule {

    private static final SettingsId DEFAULT_SETTINGS_ID = SettingsId.of("DEFAULT");

    private final SettingsService settingsService;

    private final PermissionsService permissionsService;

    private final TelegramApi telegramApi;

    private final ReactiveTransactionManager transactionManager;

    private boolean isInitialized = false;

    public TelegramModule(
            SettingsService settingsService,
            PermissionsService permissionsService,
            TelegramApi telegramApi,
            ReactiveTransactionManager transactionManager
    ) {
        this.settingsService = settingsService;
        this.permissionsService = permissionsService;
        this.telegramApi = telegramApi;
        this.transactionManager = transactionManager;
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
        return allowSystemUserToSendViaBot();
    }

    public Mono<Void> sendMessageViaBot(SendMessageViaBotRequest request, Agent agent) {
        var chatId = ChatId.of(request.chatId);
        var message = ChatMessage.of(request.text);

        return assertAgentIsAllowedOnBot(agent, SEND_MESSAGES)
                .then(getSettings())
                .flatMap(settings -> Mono.justOrEmpty(settings.getBotSettings().getApiToken()))
                .switchIfEmpty(Mono.error(new BotApiTokenMissingException()))
                .flatMap(botApiToken -> telegramApi.sendMessageViaBot(
                        chatId,
                        message,
                        botApiToken
                ));
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

    public Mono<UpdateBotApiTokenResponse> updateBotApiToken(UpdateBotApiTokenRequest request, Agent agent) {
        return assertAgentIsAllowedOnSettings(agent, UPDATE_BOT_API_TOKEN)
                .then(settingsService.updateBotApiToken(
                        DEFAULT_SETTINGS_ID,
                        Version.of(request.version),
                        ApiToken.of(request.apiToken),
                        agent
                ))
                .map(newVersion -> {
                    var response = new UpdateBotApiTokenResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<ClearBotApiTokenResponse> clearBotApiToken(ClearBotApiTokenRequest request, Agent agent) {
        return assertAgentIsAllowedOnSettings(agent, UPDATE_BOT_API_TOKEN)
                .then(settingsService.clearBotApiToken(DEFAULT_SETTINGS_ID, Version.of(request.version), agent))
                .map(newVersion -> {
                    var response = new ClearBotApiTokenResponse();
                    response.version = newVersion.getValue();
                    return response;
                });
    }

    public Mono<Void> allowSystemUserToSendViaBot() {
        var systemHolder = Holder.group(HolderId.system());

        var sendMessage = Permission.builder()
                .holder(systemHolder)
                .isAllowedTo(SEND_MESSAGES)
                .onType(getBotResourceType());

        return permissionsService.addPermission(sendMessage);
    }

    public Mono<Void> allowUserToReadAndManageSettings(String userId) {
        var userHolder = Holder.user(HolderId.of(userId));

        var readSettings = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(READ)
                .onType(getSettingsResourceType());
        var updateBotApiToken = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(UPDATE_BOT_API_TOKEN)
                .onType(getSettingsResourceType());
        var clearBotApiToken = Permission.builder()
                .holder(userHolder)
                .isAllowedTo(CLEAR_BOT_API_TOKEN)
                .onType(getSettingsResourceType());

        return permissionsService.addPermissions(
                readSettings,
                updateBotApiToken,
                clearBotApiToken
        );
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        return permissionsService.removePermissionsByHolder(Holder.user(HolderId.of(userId)));
    }

    private Mono<Settings> getSettings() {
        return settingsService.get(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(settingsService.init(DEFAULT_SETTINGS_ID, Agent.system())
                        .flatMap(idAndVersion -> settingsService.get(idAndVersion.getId())));
    }

    private Mono<Void> assertAgentIsAllowedOnBot(Agent agent, Action action) {
        Permission permission = toBotPermission(agent, action);
        return permissionsService.assertHasPermission(permission);
    }

    private Permission toBotPermission(Agent agent, Action action) {
        Holder holder = toHolder(agent);
        var resourceType = getBotResourceType();

        return Permission.builder()
                .holder(holder)
                .isAllowedTo(action)
                .onType(resourceType);
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
        return ResourceType.of("TELEGRAM_SETTINGS");
    }

    private ResourceType getBotResourceType() {
        return ResourceType.of("TELEGRAM_BOT");
    }

}
