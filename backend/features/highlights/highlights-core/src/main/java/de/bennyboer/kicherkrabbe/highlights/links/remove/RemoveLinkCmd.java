package de.bennyboer.kicherkrabbe.highlights.links.remove;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.highlights.LinkId;
import de.bennyboer.kicherkrabbe.highlights.LinkType;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RemoveLinkCmd implements Command {

    LinkType linkType;

    LinkId linkId;

    public static RemoveLinkCmd of(LinkType linkType, LinkId linkId) {
        notNull(linkType, "Link type must be given");
        notNull(linkId, "Link ID must be given");

        return new RemoveLinkCmd(linkType, linkId);
    }

}
