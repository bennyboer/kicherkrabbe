package de.bennyboer.kicherkrabbe.credentials.internal;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.credentials.internal.errors.InvalidCredentialsUsedOrUserLockedError;
import de.bennyboer.kicherkrabbe.credentials.internal.events.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.credentials.internal.password.EncodedPassword;
import de.bennyboer.kicherkrabbe.credentials.internal.password.Password;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.inmemory.InMemoryEventSourcingRepo;
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
        var result = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block();

        // then: the result contains the credentials ID and initial version
        assertThat(result.getId()).isNotNull();
        assertThat(result.getVersion()).isEqualTo(Version.zero());

        // and: the credentials can be retrieved
        var credentials = credentialsService.get(result.getId()).block();
        assertThat(credentials).isNotNull();

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
            credentialsService.create(
                    Name.of("TestUser"),
                    Password.of("1234567"),
                    UserId.of("USER_ID"),
                    system()
            ).block();
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    void shouldUseCredentialsSuccessfully() {
        clock.setNow(Instant.parse("2024-03-14T12:30:00Z"));

        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: using the credentials
        var version = credentialsService.use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        ).block();

        // then: the version is incremented
        assertThat(version).isEqualTo(Version.of(1));

        // and: the credentials are not locked
        var credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:30:00Z")));

        // and: there is no failed login attempt
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);
    }

    @Test
    void shouldRaiseErrorWhenUsingIncorrectCredentials() {
        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: using the credentials with the wrong password; then: an error is raised
        assertThatThrownBy(() -> {
            credentialsService.use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("WrongPassword"),
                    anonymous()
            ).block();
        }).matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);
    }

    @Test
    void shouldCapFailedUsageAttemptsAt999() {
        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: using the credentials with a wrong password 1000 times
        for (int i = 0; i < 1000; i++) {
            try {
                credentialsService.use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                ).block();
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are locked
        var credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the failed usage attempts are capped at 999
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(999);
    }

    @Test
    void shouldAllowUsageWhenNotHavingTooManyFailedAttempts() {
        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: using the credentials with a wrong password 5 times
        for (int i = 0; i < 5; i++) {
            try {
                credentialsService.use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                ).block();
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are not locked
        var credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the failed usage attempts are 5
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(5);

        // when: using the credentials with the correct password
        credentialsService.use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        ).block();

        // then: the failed usage attempts are reset
        credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);

        // and: the credentials are not locked
        assertThat(credentials.isLocked(clock)).isFalse();
    }

    @Test
    void shouldLockCredentialsForHalfAnHourAfterTooManyAttempts() {
        clock.setNow(Instant.parse("2024-03-14T12:30:00Z"));

        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: using the credentials with a wrong password 6 times
        for (int i = 0; i < 6; i++) {
            try {
                credentialsService.use(
                        credentialsId,
                        Name.of("TestUser"),
                        Password.of("WrongPassword"),
                        anonymous()
                ).block();
            } catch (Exception e) {
                // ignore
            }
        }

        // then: the credentials are locked
        var credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:30:00Z")));

        // and: the failed usage attempts are 6
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(6);

        // when: using the credentials with the correct password; then: an error is still raised
        clock.setNow(Instant.parse("2024-03-14T12:45:00Z"));
        assertThatThrownBy(() -> {
            credentialsService.use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("TestPassword"),
                    anonymous()
            ).block();
        }).matches(e -> e.getCause() instanceof InvalidCredentialsUsedOrUserLockedError);

        // and: the credentials are still locked
        credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isTrue();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T12:45:00Z")));

        // and: the failed usage attempts are incremented
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(7);

        // when: using the credentials with the correct password after half an hour
        clock.setNow(Instant.parse("2024-03-14T13:15:01Z"));
        credentialsService.use(
                credentialsId,
                Name.of("TestUser"),
                Password.of("TestPassword"),
                anonymous()
        ).block();

        // then: the credentials are not locked
        credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials.isLocked(clock)).isFalse();

        // and: the last used timestamp is set
        assertThat(credentials.getLastUsedAt()).isEqualTo(Optional.of(Instant.parse("2024-03-14T13:15:01Z")));

        // and: the failed usage attempts are reset
        assertThat(credentials.getFailedUsageAttempts()).isEqualTo(0);
    }

    @Test
    void shouldRaiseErrorWhenUsingDeletedCredentials() {
        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: deleting the credentials
        credentialsService.delete(credentialsId, system()).block();

        // then: the credentials are deleted
        var credentials = credentialsService.get(credentialsId).block();
        assertThat(credentials).isNull();

        // when: using the credentials
        assertThatThrownBy(() -> {
            credentialsService.use(
                    credentialsId,
                    Name.of("TestUser"),
                    Password.of("TestPassword"),
                    anonymous()
            ).block();
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot apply command to deleted aggregate");
    }

    @Test
    void shouldCollapseEventsOnDeleteAndDeletePassword() {
        // given: a set of credentials
        var credentialsId = credentialsService.create(
                Name.of("TestUser"),
                Password.of("TestPassword"),
                UserId.of("USER_ID"),
                system()
        ).block().getId();

        // when: deleting the credentials
        credentialsService.delete(credentialsId, system()).block();

        // then: the credentials are deleted
        var credentials = credentialsService.get(credentialsId).block();
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
        assertThat(event.getEvent().isSnapshot()).isTrue();

        // and: the event does not contain the password or name
        SnapshottedEvent e = (SnapshottedEvent) event.getEvent();
        assertThat(e.getName()).isEqualTo(Name.of("ANONYMIZED"));
        assertThat(e.getEncodedPassword()).isEqualTo(EncodedPassword.of("ANONYMIZED"));
    }

}
