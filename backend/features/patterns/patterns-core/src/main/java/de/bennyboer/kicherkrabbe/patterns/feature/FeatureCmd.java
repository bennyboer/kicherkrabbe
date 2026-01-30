package de.bennyboer.kicherkrabbe.patterns.feature;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class FeatureCmd implements Command {

    public static FeatureCmd of() {
        return new FeatureCmd();
    }

}
