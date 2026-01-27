package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value(staticConstructor = "of")
public class SnapshotCmd implements Command {

}
