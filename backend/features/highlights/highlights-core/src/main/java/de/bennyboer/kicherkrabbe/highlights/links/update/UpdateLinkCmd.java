package de.bennyboer.kicherkrabbe.highlights.links.update;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.highlights.Link;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UpdateLinkCmd implements Command {

    Link link;

    public static UpdateLinkCmd of(Link link) {
        notNull(link, "Link must be given");

        return new UpdateLinkCmd(link);
    }

}
