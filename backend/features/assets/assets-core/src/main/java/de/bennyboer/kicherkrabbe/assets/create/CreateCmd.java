package de.bennyboer.kicherkrabbe.assets.create;

import de.bennyboer.kicherkrabbe.assets.ContentType;
import de.bennyboer.kicherkrabbe.assets.Location;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    ContentType contentType;

    Location location;

    public static CreateCmd of(ContentType contentType, Location location) {
        notNull(contentType, "Content type must be given");
        notNull(location, "Location must be given");

        return new CreateCmd(contentType, location);
    }

}
