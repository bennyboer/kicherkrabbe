package de.bennyboer.kicherkrabbe.highlights;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Link {

    LinkType type;

    LinkId id;

    LinkName name;

    public static Link of(LinkType type, LinkId id, LinkName name) {
        notNull(type, "Type must be given");
        notNull(id, "Id must be given");
        notNull(name, "Name must be given");

        return new Link(type, id, name);
    }

    @Override
    public String toString() {
        return "Link(type=%s, id=%s, name=%s)".formatted(type, id, name);
    }

}
