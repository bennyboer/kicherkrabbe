package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UsersServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher publisher = new LoggingEventPublisher();

    private final UsersService service = new UsersService(repo, publisher, Clock.systemUTC());

    @Test
    void shouldCreateUser() {
        // when: creating a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // then: the user can be found
        var user = get(id);
        assertThat(user).isNotNull();

        // and: the user has the correct name
        assertThat(user.getName().getFirstName().getValue()).isEqualTo("Max");
        assertThat(user.getName().getLastName().getValue()).isEqualTo("Mustermann");

        // and: the user has the correct mail
        assertThat(user.getMail().getValue()).isEqualTo("max.mustermann@kicherkrabbe.com");
    }

    @Test
    void shouldRenameUser() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // when: renaming the user
        rename(
                id,
                Version.zero(),
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        );

        // then: the user has the new name
        var user = get(id);
        assertThat(user.getName().getFirstName().getValue()).isEqualTo("Maria");
        assertThat(user.getName().getLastName().getValue()).isEqualTo("Mustermann");
    }

    @Test
    void shouldNotRenameUserGivenAnOutdatedVersion() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // and: the user is renamed
        rename(
                id,
                Version.zero(),
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        );

        // when: renaming the user with an outdated version; then: an error is raised
        assertThatThrownBy(() -> rename(
                id,
                Version.zero(),
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseErrorWhenTryingToRenameDeletedUser() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // and: the user is deleted
        var version = delete(id, Version.zero(), Agent.system());

        // when: renaming the credentials; then: an error is raised
        assertThatThrownBy(() -> rename(
                id,
                version,
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot apply command to deleted aggregate");
    }

    @Test
    void shouldCollapseEventsOnDeleteAndAnonymizePersonalData() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // when: deleting the user
        delete(id, Version.zero(), Agent.system());

        // then: the user is deleted and cannot be found anymore
        var user = get(id);
        assertThat(user).isNull();

        // and: there is only a single event in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                User.TYPE,
                Version.zero()
        ).collectList().block();

        assertThat(events).hasSize(1);
        var event = events.getFirst();
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the event does not contain the name or mail
        SnapshotEvent e = (SnapshotEvent) event.getEvent();
        var state = e.getState();
        @SuppressWarnings("unchecked")
        var name = (Map<String, Object>) state.get("name");
        assertThat(name.get("firstName")).isEqualTo("ANONYMIZED");
        assertThat(name.get("lastName")).isEqualTo("ANONYMIZED");
        assertThat(state.get("mail")).isEqualTo("anonymized@kicherkrabbe.com");
    }

    @Test
    void shouldNotDeleteUserGivenAnOutdatedVersion() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // and: the user is renamed
        rename(
                id,
                Version.zero(),
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        );

        // when: deleting the user with an outdated version; then: an error is raised
        assertThatThrownBy(() -> delete(
                id,
                Version.zero(),
                Agent.system()
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a user
        var id = create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        );

        // when: the user is renamed 200 times
        var version = Version.zero();
        for (int i = 0; i < 200; i++) {
            version = rename(
                    id,
                    version,
                    FullName.of(
                            FirstName.of("Maria " + i),
                            LastName.of("Mustermann")
                    ),
                    Agent.system()
            );
        }

        // then: the user is updated
        var user = get(id);
        assertThat(user.getVersion()).isEqualTo(Version.of(202));
        assertThat(user.getName().getFirstName().getValue()).isEqualTo("Maria 199");

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(id.getValue()),
                User.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private User get(UserId id) {
        return service.get(id).block();
    }

    private UserId create(FullName name, Mail mail, Agent agent) {
        return service.create(name, mail, agent).block().getId();
    }

    private Version rename(UserId id, Version version, FullName name, Agent agent) {
        return service.rename(id, version, name, agent).block();
    }

    private Version delete(UserId id, Version version, Agent agent) {
        return service.delete(id, version, agent).block();
    }

}
