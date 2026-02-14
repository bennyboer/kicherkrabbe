package de.bennyboer.kicherkrabbe.credentials;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.credentials.create.CreateCmd;
import de.bennyboer.kicherkrabbe.credentials.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.credentials.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.credentials.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.credentials.use.UsageFailedEvent;
import de.bennyboer.kicherkrabbe.credentials.use.UsageSucceededEvent;
import de.bennyboer.kicherkrabbe.credentials.use.UseCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static java.time.temporal.ChronoUnit.MINUTES;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Credentials implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("CREDENTIALS");

    @SnapshotExclude
    CredentialsId id;

    @SnapshotExclude
    Version version;

    Name name;

    EncodedPassword encodedPassword;

    UserId userId;

    int failedUsageAttempts;

    @Nullable
    @Getter(NONE)
    Instant lastUsedAt;

    @Nullable
    @Getter(NONE)
    Instant createdAt;

    @Nullable
    @Getter(NONE)
    Instant deletedAt;

    public static Credentials init() {
        return new Credentials(
                null,
                Version.zero(),
                null,
                null,
                null,
                0,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    encodePassword(c.getPassword()),
                    c.getUserId()
            ));
            case UseCmd c -> ApplyCommandResult.of(tryUse(c.getName(), c.getPassword(), c.getClock()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        CredentialsId id = CredentialsId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withEncodedPassword(e.getEncodedPassword())
                    .withUserId(e.getUserId())
                    .withCreatedAt(metadata.getDate());
            case UsageFailedEvent e -> withFailedUsageAttempts(Math.min(failedUsageAttempts + 1, 999))
                    .withLastUsedAt(e.getDate());
            case UsageSucceededEvent e -> withFailedUsageAttempts(0)
                    .withLastUsedAt(e.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate())
                    .withName(Name.of("ANONYMIZED"))
                    .withEncodedPassword(EncodedPassword.of("ANONYMIZED"));
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public Optional<Instant> getLastUsedAt() {
        return Optional.ofNullable(lastUsedAt);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public Optional<Instant> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public boolean isDeleted() {
        return getDeletedAt().isPresent();
    }

    public boolean isNotDeleted() {
        return !isDeleted();
    }

    public boolean isLocked(Clock clock) {
        if (isDeleted()) {
            return true;
        }

        return hasTooManyFailedAttempts() && hasBeenUsedRecently(clock);
    }

    public boolean hasFailedAttempts() {
        return failedUsageAttempts > 0;
    }

    private boolean hasBeenUsedRecently(Clock clock) {
        return getLastUsedAt()
                .map(lastUsedAt -> !clock.instant().isAfter(lastUsedAt.plus(30, MINUTES)))
                .orElse(false);
    }

    private boolean hasTooManyFailedAttempts() {
        return failedUsageAttempts > 5;
    }

    private EncodedPassword encodePassword(Password password) {
        String rawPassword = password.getValue();
        String rawEncodedPassword = PasswordEncoder.getInstance().encode(rawPassword);

        return EncodedPassword.of(rawEncodedPassword);
    }

    private boolean matchesEncodedPassword(Password password) {
        String rawPassword = password.getValue();
        String rawEncodedPassword = encodedPassword.getValue();

        return PasswordEncoder.getInstance().matches(rawPassword, rawEncodedPassword);
    }

    private Event tryUse(Name name, Password password, Clock clock) {
        if (isLocked(clock)) {
            return UsageFailedEvent.of(clock.instant());
        }

        boolean matchesName = name.equals(this.name);
        if (matchesName && matchesEncodedPassword(password)) {
            return UsageSucceededEvent.of(clock.instant());
        }

        return UsageFailedEvent.of(clock.instant());
    }

    private boolean isCreated() {
        return createdAt != null;
    }

}
