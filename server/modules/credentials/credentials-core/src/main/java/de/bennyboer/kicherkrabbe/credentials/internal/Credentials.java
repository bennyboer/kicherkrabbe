package de.bennyboer.kicherkrabbe.credentials.internal;

import de.bennyboer.kicherkrabbe.auth.password.PasswordEncoder;
import de.bennyboer.kicherkrabbe.credentials.internal.commands.CreateCmd;
import de.bennyboer.kicherkrabbe.credentials.internal.commands.DeleteCmd;
import de.bennyboer.kicherkrabbe.credentials.internal.commands.UseCmd;
import de.bennyboer.kicherkrabbe.credentials.internal.events.*;
import de.bennyboer.kicherkrabbe.credentials.internal.password.EncodedPassword;
import de.bennyboer.kicherkrabbe.credentials.internal.password.Password;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
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

    CredentialsId id;

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
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getName(),
                    getEncodedPassword(),
                    getUserId(),
                    getFailedUsageAttempts(),
                    getLastUsedAt().orElse(null),
                    getDeletedAt().orElse(null)
            ));
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
            case SnapshottedEvent e -> withId(id)
                    .withName(e.getName())
                    .withEncodedPassword(e.getEncodedPassword())
                    .withUserId(e.getUserId())
                    .withFailedUsageAttempts(e.getFailedUsageAttempts())
                    .withLastUsedAt(e.getLastUsedAt().orElse(null))
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withEncodedPassword(e.getEncodedPassword())
                    .withUserId(e.getUserId());
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

}
