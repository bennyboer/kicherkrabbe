package de.bennyboer.kicherkrabbe.telegram.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.clear.BotApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.clear.ClearBotApiTokenCmd;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update.BotApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update.UpdateBotApiTokenCmd;
import de.bennyboer.kicherkrabbe.telegram.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.telegram.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.telegram.settings.snapshot.SnapshottedEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Settings implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("TELEGRAM_SETTINGS");

    SettingsId id;

    Version version;

    BotSettings botSettings;

    public static Settings init() {
        return new Settings(
                null,
                Version.zero(),
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(getBotSettings()));
            case InitCmd c -> ApplyCommandResult.of(InitEvent.of(c.getBotSettings()));
            case UpdateBotApiTokenCmd c -> ApplyCommandResult.of(BotApiTokenUpdatedEvent.of(c.getApiToken()));
            case ClearBotApiTokenCmd ignored -> ApplyCommandResult.of(BotApiTokenClearedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = SettingsId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withBotSettings(e.getBotSettings());
            case InitEvent e -> withId(id)
                    .withBotSettings(e.getBotSettings());
            case BotApiTokenUpdatedEvent e -> withBotSettings(getBotSettings().updateApiToken(e.getApiToken()));
            case BotApiTokenClearedEvent ignored -> withBotSettings(getBotSettings().clearApiToken());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

}
