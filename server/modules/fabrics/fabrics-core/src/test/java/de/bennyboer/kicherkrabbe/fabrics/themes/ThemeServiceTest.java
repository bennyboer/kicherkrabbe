package de.bennyboer.kicherkrabbe.fabrics.themes;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThemeServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final ThemeService themeService = new ThemeService(repo, eventPublisher);

    @Test
    void shouldCreateTheme() {
        // given: a name to create a theme for
        var name = ThemeName.of("Autumn");

        // when: creating a theme
        var id = create(name);

        // then: the theme is created
        var theme = get(id);
        assertThat(theme.getId()).isEqualTo(id);
        assertThat(theme.getVersion()).isEqualTo(Version.zero());
        assertThat(theme.getName()).isEqualTo(name);
        assertThat(theme.isNotDeleted()).isTrue();
    }

    @Test
    void shouldUpdateTheme() {
        // given: a theme
        var id = create(ThemeName.of("Summer"));

        // when: updating the theme
        var updatedVersion = update(id, Version.zero(), ThemeName.of("Fall"));

        // then: the theme is updated
        var theme = get(id);
        assertThat(theme.getId()).isEqualTo(id);
        assertThat(theme.getVersion()).isEqualTo(updatedVersion);
        assertThat(theme.getName()).isEqualTo(ThemeName.of("Fall"));
        assertThat(theme.isNotDeleted()).isTrue();
    }

    @Test
    void shouldNotUpdateThemeGivenAnOutdatedVersion() {
        // given: a theme
        var id = create(ThemeName.of("Winter"));

        // and: the theme is updated
        update(id, Version.zero(), ThemeName.of("Spring"));

        // when: updating the theme with an outdated version; then: an error is raised
        assertThatThrownBy(() -> update(id, Version.zero(), ThemeName.of("Summer")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeleteTheme() {
        // given: a theme
        var id = create(ThemeName.of("Winter"));

        // when: deleting the theme
        delete(id, Version.zero());

        // then: the theme is deleted
        assertThat(get(id)).isNull();
    }

    @Test
    void shouldNotDeleteThemeGivenAnOutdatedVersion() {
        // given: a theme
        var id = create(ThemeName.of("Spring"));

        // and: the theme is updated
        update(id, Version.zero(), ThemeName.of("Summer"));

        // when: deleting the theme with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(id, Version.zero()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a theme
        var id = create(ThemeName.of("Spring"));

        // when: updating the theme 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = update(id, version, ThemeName.of("Summer " + i));
        }

        // then: the theme is updated
        var theme = get(id);
        assertThat(theme.getVersion()).isEqualTo(Version.of(202));
        assertThat(theme.getName()).isEqualTo(ThemeName.of("Summer 199"));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                Theme.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Theme get(ThemeId id) {
        return themeService.get(id).block();
    }

    private ThemeId create(ThemeName name) {
        return themeService.create(name, Agent.system()).block().getId();
    }

    private Version update(ThemeId id, Version version, ThemeName name) {
        return themeService.update(id, version, name, Agent.system()).block();
    }

    private void delete(ThemeId id, Version version) {
        themeService.delete(id, version, Agent.system()).block();
    }

}
