package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.highlights.Link;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class LinkPage {

    long total;

    List<Link> links;

    public static LinkPage of(long total, List<Link> links) {
        notNull(links, "Links must be given");

        return new LinkPage(total, links);
    }

}
