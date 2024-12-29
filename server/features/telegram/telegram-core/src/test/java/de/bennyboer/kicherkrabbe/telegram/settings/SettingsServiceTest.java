package de.bennyboer.kicherkrabbe.telegram.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SettingsServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final SettingsService settingsService = new SettingsService(
            repo,
            eventPublisher,
            Clock.systemUTC()
    );

    @Test
    void shouldInitSettings() {
        // when: initializing settings
        var settingsId = init();

        // then: the settings are initialized
        var settings = get(settingsId);
        assertThat(settings.getId()).isEqualTo(settingsId);
        assertThat(settings.getVersion()).isEqualTo(Version.zero());
        assertThat(settings.getBotSettings()).isEqualTo(BotSettings.init());
    }

    @Test
    void shouldUpdateBotApiToken() {
        // given: initialized settings
        var settingsId = init();

        // when: enabling system notifications
        var version = updateBotApiToken(settingsId, Version.zero(), ApiToken.of("SOME_API_TOKEN"));

        // then: the bot API token is updated
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getBotSettings().getApiToken()).isEqualTo(Optional.of(ApiToken.of("SOME_API_TOKEN")));
    }

    @Test
    void shouldNotUpdateBotApiTokenGivenAnOutdatedVersion() {
        // given: some initialized settings
        var settingsId = init();
        updateBotApiToken(settingsId, Version.zero(), ApiToken.of("SOME_API_TOKEN"));

        // when: updating bot API token with an outdated version
        assertThatThrownBy(() -> updateBotApiToken(settingsId, Version.zero(), ApiToken.of("SOME_API_TOKEN")))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldClearBotApiToken() {
        // given: initialized settings
        var settingsId = init();
        var version = updateBotApiToken(settingsId, Version.zero(), ApiToken.of("SOME_API_TOKEN"));

        // when: clearing the bot API token
        var newVersion = clearBotApiToken(settingsId, version);

        // then: the bot API token is cleared
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(newVersion);
        assertThat(settings.getBotSettings().getApiToken()).isEmpty();
    }

    @Test
    void shouldNotClearBotApiTokenGivenAnOutdatedVersion() {
        // given: some initialized settings
        var settingsId = init();
        updateBotApiToken(settingsId, Version.zero(), ApiToken.of("SOME_API_TOKEN"));
        updateBotApiToken(settingsId, Version.of(1), ApiToken.of("SOME_API_TOKEN_2"));

        // when: clearing bot API token with an outdated version
        assertThatThrownBy(() -> clearBotApiToken(settingsId, Version.of(1)))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorWhenTryingToClearAlreadyClearedApiToken() {
        // given: initialized settings
        var settingsId = init();

        // when: clearing the bot API token; then: an error is raised
        assertThatThrownBy(() -> clearBotApiToken(settingsId, Version.zero()))
                .matches(e -> e instanceof BotApiTokenAlreadyClearedException);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: initialized settings
        var settingsId = init();

        // when: updating the bot API token 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = updateBotApiToken(settingsId, version, ApiToken.of("SOME_API_TOKEN_" + (i + 1)));
        }

        // then: the bot API token is updated
        var settings = get(settingsId);
        assertThat(settings.getVersion()).isEqualTo(Version.of(202));
        assertThat(settings.getBotSettings().getApiToken()).isEqualTo(Optional.of(ApiToken.of("SOME_API_TOKEN_200")));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(settingsId.getValue()),
                Settings.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private SettingsId init() {
        return settingsService.init(Agent.system()).block().getId();
    }

    private Settings get(SettingsId id) {
        return settingsService.get(id).block();
    }

    private Version updateBotApiToken(SettingsId id, Version version, ApiToken apiToken) {
        return settingsService.updateBotApiToken(id, version, apiToken, Agent.system()).block();
    }

    private Version clearBotApiToken(SettingsId id, Version version) {
        return settingsService.clearBotApiToken(id, version, Agent.system()).block();
    }

}
