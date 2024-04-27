package de.bennyboer.kicherkrabbe.users.internal;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.users.internal.snapshot.SnapshottedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UsersServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher publisher = new LoggingEventPublisher();

    private final UsersService service = new UsersService(repo, publisher);

    @Test
    void shouldCreateUser() {
        // when: creating a user
        var id = service.create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        ).block().getId();

        // then: the user can be found
        var user = service.get(id).block();
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
        var id = service.create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        ).block().getId();

        // when: renaming the user
        service.rename(
                id,
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        ).block();

        // then: the user has the new name
        var user = service.get(id).block();
        assertThat(user.getName().getFirstName().getValue()).isEqualTo("Maria");
        assertThat(user.getName().getLastName().getValue()).isEqualTo("Mustermann");
    }

    @Test
    void shouldRaiseErrorWhenTryingToRenameDeletedUser() {
        // given: a user
        var id = service.create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        ).block().getId();

        // and: the user is deleted
        service.delete(id, Agent.system()).block();

        // when: renaming the credentials; then: an error is raised
        assertThatThrownBy(() -> service.rename(
                id,
                FullName.of(
                        FirstName.of("Maria"),
                        LastName.of("Mustermann")
                ),
                Agent.system()
        ).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot apply command to deleted aggregate");
    }

    @Test
    void shouldCollapseEventsOnDeleteAndAnonymizePersonalData() {
        // given: a user
        var id = service.create(
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com"),
                Agent.system()
        ).block().getId();

        // when: deleting the user
        service.delete(id, Agent.system()).block();

        // then: the user is deleted and cannot be found anymore
        var user = service.get(id).block();
        assertThat(user).isNull();

        // and: there is only a single event in the repository
        var events = repo.findEventsByAggregateIdAndType(
                        AggregateId.of(id.getValue()),
                        User.TYPE,
                        Version.zero()
                )
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        var event = events.getFirst();
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the event does not contain the name or mail
        SnapshottedEvent e = (SnapshottedEvent) event.getEvent();
        assertThat(e.getName().getFirstName().getValue()).isEqualTo("ANONYMIZED");
        assertThat(e.getName().getLastName().getValue()).isEqualTo("ANONYMIZED");
        assertThat(e.getMail().getValue()).isEqualTo("anonymized@kicherkrabbe.com");
    }

}
