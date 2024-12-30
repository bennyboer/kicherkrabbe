package de.bennyboer.kicherkrabbe.telegram;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.permissions.persistence.inmemory.InMemoryPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import de.bennyboer.kicherkrabbe.telegram.api.requests.ClearBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
import de.bennyboer.kicherkrabbe.telegram.api.requests.UpdateBotApiTokenRequest;
import de.bennyboer.kicherkrabbe.telegram.api.responses.ClearBotApiTokenResponse;
import de.bennyboer.kicherkrabbe.telegram.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.telegram.api.responses.UpdateBotApiTokenResponse;
import de.bennyboer.kicherkrabbe.telegram.external.LoggingTelegramApi;
import de.bennyboer.kicherkrabbe.telegram.settings.SettingsService;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Clock;

public class TelegramModuleTest {

    private final TelegramModuleConfig config = new TelegramModuleConfig();

    private final SettingsService settingsService = new SettingsService(
            new InMemoryEventSourcingRepo(),
            new LoggingEventPublisher(),
            Clock.systemUTC()
    );

    private final PermissionsService permissionsService = new PermissionsService(
            new InMemoryPermissionsRepo(),
            event -> Mono.empty()
    );

    protected final LoggingTelegramApi telegramApi = new LoggingTelegramApi();

    private final ReactiveTransactionManager transactionManager = new MockReactiveTransactionManager();

    private final TelegramModule module = config.telegramModule(
            settingsService,
            permissionsService,
            telegramApi,
            transactionManager
    );

    public void sendMessageViaBot(SendMessageViaBotRequest request, Agent agent) {
        module.sendMessageViaBot(request, agent).block();
    }

    public QuerySettingsResponse getSettings(Agent agent) {
        return module.getSettings(agent).block();
    }

    public UpdateBotApiTokenResponse updateBotApiToken(UpdateBotApiTokenRequest request, Agent agent) {
        return module.updateBotApiToken(request, agent).block();
    }

    public ClearBotApiTokenResponse clearBotApiToken(ClearBotApiTokenRequest request, Agent agent) {
        return module.clearBotApiToken(request, agent).block();
    }

    public void allowSystemUserToSendMessagesViaBot() {
        module.allowSystemUserToSendViaBot().block();
    }

    public void allowUserToReadAndManageSettings(String userId) {
        module.allowUserToReadAndManageSettings(userId).block();
    }

}
