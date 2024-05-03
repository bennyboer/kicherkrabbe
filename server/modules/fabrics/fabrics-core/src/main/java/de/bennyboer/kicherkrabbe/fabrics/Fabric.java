package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrics.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.themes.ThemeId;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Fabric implements Aggregate {

    FabricId id;

    Version version;

    ImageId image;

    Set<Color> colors;

    Set<ThemeId> themes;

    // TODO Availability

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return null; // TODO
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return null; // TODO
    }

}
