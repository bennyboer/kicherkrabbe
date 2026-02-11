package de.bennyboer.kicherkrabbe.highlights;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.HashSet;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Links {

    Set<Link> links;

    public static Links of(Set<Link> links) {
        notNull(links, "Links must be given");

        return new Links(links);
    }

    public Links add(Link link) {
        var updatedLinks = new HashSet<>(links);
        updatedLinks.add(link);

        return withLinks(updatedLinks);
    }

    public Links remove(LinkType linkType, LinkId linkId) {
        var updatedLinks = new HashSet<>(links);
        updatedLinks.removeIf(link -> link.getType().equals(linkType) && link.getId().equals(linkId));

        return withLinks(updatedLinks);
    }

    public Links update(Link link) {
        var updatedLinks = new HashSet<>(links);
        updatedLinks.removeIf(existingLink ->
                existingLink.getType().equals(link.getType()) && existingLink.getId().equals(link.getId()));
        updatedLinks.add(link);

        return withLinks(updatedLinks);
    }

    public boolean contains(LinkType linkType, LinkId linkId) {
        return links.stream().anyMatch(link -> link.getType().equals(linkType) && link.getId().equals(linkId));
    }

}
