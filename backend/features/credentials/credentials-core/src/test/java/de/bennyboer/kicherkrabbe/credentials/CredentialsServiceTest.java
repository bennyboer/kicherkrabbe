package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.credentials.use.InvalidCredentialsUsedOrUserLockedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent.anonymous;
import static de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent.system;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CredentialsServiceTest {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final LoggingEventPublisher eventPublisher = new LoggingEventPublisher();

    private final TestClock clock = new TestClock();

    private final CredentialsService credentialsService = new CredentialsService(repo, eventPublisher, clock);

    @BeforeEach
    public void setUp() {
        PasswordEncoder.getInstance().enableTestProfile();
    }

    @Test
    void shouldCreateCredentials() {
        // when: creating credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // then: the result contains the credentials ID
        assertThat(credentialsId).isNotNull();

        // and: the credentials can be retrieved
        var credentials = get(credentialsId);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getId()).isEqualTo(credentialsId);
        assertThat(credentials.getVersion()).isEqualTo(Version.zero());

        // and: the name and user ID are set
        assertThat(credentials.getName()).isEqualTo(Name.of("TestUser"));
        assertThat(credentials.getUserId()).isEqualTo(UserId.of("USER_ID"));

        // and: the password is properly encoded
        assertThat(PasswordEncoder.getInstance().matches(
                "TestPassword",
                credentials.getEncodedPassword().getValue()
        )).isTrue();

        // and: the credentials are not deleted
        assertThat(credentials.isDeleted()).isFalse();

        // and: the credentials are not locked
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the last used timestamp is not set
        assertThat(credentials.getLastUsedAt()).isEmpty();

        // and: there is no failed login attempt
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);
    }

    @Test
    void shouldNotAcceptPasswordBelowMinimumLength() {
        assertThatThrownBy(() -> {
            create(
                    Name.of("TestUser"),
                    Password.of("1234567"),
                    UserId.of("USER_ID"),
                    system()
            );
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    void shouldUseCredentialsSuccessfully() {
        clock.setNow(Instant.parse("2024-03-14T12:30:00Z"));

        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: using the credentials
        var version = use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        );

        // then: the version is incremented
        assertThat(version).isEqualTo(Version.of(1));

        // and: the credentials are not locked
        var credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:30:00Z")));

        // and: there is no failed login attempt
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);
    }

    @Test
    void shouldRaiseErrorWhenUsingIncorrectCredentials() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: using the credentials with the wrong password; then: an error is raised
        assertThatThrownBy(() -> {
            use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("WrongPassword"),
                    anonymous()
            );
        }).matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);
    }

    @Test
    void shouldCapFailedUsageAttemptsAt999() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: using the credentials with a wrong password 1000 times
        for (int i = 0; i < 1000; i++) {
            try {
                use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                );
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are locked
        var credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the failed usage attempts are capped at 999
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(999);
    }

    @Test
    void shouldAllowUsageWhenNotHavingTooManyFailedAttempts() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: using the credentials with a wrong password 5 times
        for (int i = 0; i < 5; i++) {
            try {
                use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                );
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are not locked
        var credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the failed usage attempts are 5
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(5);

        // when: using the credentials with the correct password
        use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        );

        // then: the failed usage attempts are reset
        credentials = get(credentialsId);
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);

        // and: the credentials are not locked
        assertThat(credentials.isLocked(clock)).isFalse();
    }

    @Test
    void shouldLockCredentialsForHalfAnHourAfterTooManyAttempts() {
        clock.setNow(Instant.parse("2024-03-14T12:30:00Z"));

        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: using the credentials with a wrong password 6 times
        for (int i = 0; i < 6; i++) {
            try {
                use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                );
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are locked
        var credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:30:00Z")));

        // and: the failed usage attempts are 6
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(6);

        // when: using the credentials with the correct password; then: an error is still raised
        clock.setNow(Instant.parse("2024-03-14T12:45:00Z"));
        assertThatThrownBy(() -> {
            use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("TestPassword"),
                    anonymous()
            );
        }).matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);

        // and: the credentials are still locked
        credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:45:00Z")));

        // and: the failed usage attempts are incremented
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(7);

        // when: using the credentials with the correct password after half an hour
        clock.setNow(Instant.parse("2024-03-14T13:15:01Z"));
        use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        );

        // then: the credentials are not locked
        credentials = get(credentialsId);
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T13:15:01Z")));

        // and: the failed usage attempts are reset
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);
    }

    @Test
    void shouldRaiseErrorWhenUsingDeletedCredentials() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: deleting the credentials
        delete(credentialsId, Version.zero(), system());

        // then: the credentials are deleted
        var credentials = get(credentialsId);
        assertThat(credentials).isNull();

        // when: using the credentials
        assertThatThrownBy(() -> {
            use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("TestPassword"),
                    anonymous()
            );
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot apply command to deleted aggregate");
    }

    @Test
    void shouldCollapseEventsOnDeleteAndDeletePassword() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: deleting the credentials
        delete(credentialsId, Version.zero(), system());

        // then: the credentials are deleted
        var credentials = get(credentialsId);
        assertThat(credentials).isNull();

        // and: there is only a single event in the repository
        var events = repo.findEventsByAggregateIdAndType(
                        AggregateId.of(credentialsId.getValue()),
                        Credentials.TYPE,
                        Version.zero()
                )
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        var event = events.getFirst();
        assertThat(event.getMetadata().isSnapshot()).isTrue();

        // and: the event does not contain the password or name
        SnapshotEvent e = (SnapshotEvent) event.getEvent();
        var state = e.getState();
        assertThat(state.get("name")).isEqualTo("ANONYMIZED");
        assertThat(state.get("encodedPassword")).isEqualTo("ANONYMIZED");
    }

    @Test
    void shouldNotDeleteCredentialsGivenAnOutdatedVersion() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // and: the credentials are used
        use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        );

        // when: deleting the credentials with an outdated version
        assertThatThrownBy(() -> {
            delete(credentialsId, Version.zero(), system());
        }).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        // given: a set of credentials
        var credentialsId = create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        );

        // when: the credentials are used 200 times
        for (int i = 0; i < 200; i++) {
            use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("TestPassword"),
                    anonymous()
            );
        }

        // then: the credentials are updated
        var credentials = get(credentialsId);
        assertThat(credentials.getVersion()).isEqualTo(Version.of(202));

        // and: there are exactly 2 snapshot events in the repository
        var events = repo.findEventsByAggregateIdAndType(
                AggregateId.of(credentialsId.getValue()),
                Credentials.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshotEvents = events.stream().filter(e -> e.getMetadata().isSnapshot()).toList();
        assertThat(snapshotEvents).hasSize(2);
        assertThat(snapshotEvents.getFirst().getMetadata().getAggregateVersion()).isEqualTo(Version.of(100));
        assertThat(snapshotEvents.getLast().getMetadata().getAggregateVersion()).isEqualTo(Version.of(200));
    }

    private Credentials get(CredentialsId id) {
        return credentialsService.get(id).block();
    }

    private Version use(CredentialsId id, Name name, Password password, Agent agent) {
        return credentialsService.use(id, name, password, agent).block();
    }

    private CredentialsId create(Name name, Password password, UserId userId, Agent agent) {
        return credentialsService.create(name, password, userId, agent).block().getId();
    }

    private Version delete(CredentialsId credentialsId, Version version, Agent agent) {
        return credentialsService.delete(credentialsId, version, agent).block();
    }

}
