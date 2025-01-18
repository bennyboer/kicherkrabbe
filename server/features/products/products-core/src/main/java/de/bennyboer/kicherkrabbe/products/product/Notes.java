package de.bennyboer.kicherkrabbe.products.product;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Notes {

    Note contains;

    Note care;

    Note safety;

    public static Notes of(Note contains, Note care, Note safety) {
        notNull(contains, "Contains note must be given");
        notNull(care, "Care note must be given");
        notNull(safety, "Safety note must be given");

        return new Notes(contains, care, safety);
    }

}
