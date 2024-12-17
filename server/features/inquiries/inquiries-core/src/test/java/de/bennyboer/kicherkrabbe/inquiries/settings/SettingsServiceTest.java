package de.bennyboer.kicherkrabbe.inquiries.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SettingsServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final SettingsService settingsService = new SettingsService(repo, eventPublisher);

    @Test
    void shouldInitSettings() {
        // when: initializing the settings
        var id = init();

        // then: the settings are initialized
        var settings = get(id);
        assertThat(settings.getId()).isEqualTo(id);
        assertThat(settings.getVersion()).isEqualTo(Version.zero());
        assertThat(settings.isEnabled()).isFalse();
        assertThat(settings.getRateLimits()).isEqualTo(RateLimits.init());
    }

    @Test
    void shouldEnableInquiries() {
        // given: initialized settings
        var id = init();

        // when: enabling inquiries
        var version = enable(id, Version.zero());

        // then: the inquiries are enabled
        var settings = get(id);
        assertThat(settings.isEnabled()).isTrue();
        assertThat(settings.getVersion()).isEqualTo(version);
    }

    @Test
    void shouldNotEnableInquiriesGivenAnOutdatedVersion() {
        // given: initialized settings
        var id = init();
        enable(id, Version.zero());

        // when: enabling inquiries with an outdated version; then: an error is raised
        assertThatThrownBy(() -> enable(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldDisableInquiries() {
        // given: enabled inquiries
        var id = init();
        var version = enable(id, Version.zero());

        // when: disabling inquiries
        version = disable(id, version);

        // then: the inquiries are disabled
        var settings = get(id);
        assertThat(settings.isEnabled()).isFalse();
        assertThat(settings.getVersion()).isEqualTo(version);
    }

    @Test
    void shouldNotDisableInquiriesGivenAnOutdatedVersion() {
        // given: enabled inquiries
        var id = init();
        enable(id, Version.zero());

        // when: disabling inquiries with an outdated version; then: an error is raised
        assertThatThrownBy(() -> disable(id, Version.zero()))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldUpdateRateLimits() {
        // given: initialized settings
        var id = init();

        // when: updating the rate limits
        var version = updateRateLimits(
                id, Version.zero(), RateLimits.of(
                        RateLimit.of(10, Duration.ofHours(48)),
                        RateLimit.of(100, Duration.ofHours(1)),
                        RateLimit.of(1000, Duration.ofHours(72))
                )
        );

        // then: the rate limits are updated
        var settings = get(id);
        assertThat(settings.getVersion()).isEqualTo(version);
        assertThat(settings.getRateLimits()).isEqualTo(
                RateLimits.of(
                        RateLimit.of(10, Duration.ofHours(48)),
                        RateLimit.of(100, Duration.ofHours(1)),
                        RateLimit.of(1000, Duration.ofHours(72))
                )
        );
    }

    @Test
    void shouldNotUpdateRateLimitsGivenAnOutdatedVersion() {
        // given: initialized settings
        var id = init();
        enable(id, Version.zero());

        // when: updating the rate limits with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateRateLimits(
                id, Version.zero(), RateLimits.init()
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: some settings
        var id = init();

        // when: updating the rate limits 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = updateRateLimits(
                    id, version, RateLimits.of(
                            RateLimit.of(10, Duration.ofHours(48)),
                            RateLimit.of(100, Duration.ofHours(1)),
                            RateLimit.of(1000, Duration.ofHours(72))
                    )
            );
        }

        // then: the rate limits are updated
        var settings = get(id);
        assertThat(settings.getVersion()).isEqualTo(Version.of(202));
        assertThat(settings.getRateLimits()).isEqualTo(RateLimits.of(
                RateLimit.of(10, Duration.ofHours(48)),
                RateLimit.of(100, Duration.ofHours(1)),
                RateLimit.of(1000, Duration.ofHours(72))
        ));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
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

    private Version enable(SettingsId id, Version version) {
        return settingsService.enable(id, version, Agent.system()).block();
    }

    private Version disable(SettingsId id, Version version) {
        return settingsService.disable(id, version, Agent.system()).block();
    }

    private Version updateRateLimits(SettingsId id, Version version, RateLimits rateLimits) {
        return settingsService.updateRateLimits(id, version, rateLimits, Agent.system()).block();
    }

}
