package de.bennyboer.kicherkrabbe.embroideries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EmbroideryCategory {

    EmbroideryCategoryId id;

    EmbroideryCategoryName name;

    public static EmbroideryCategory of(EmbroideryCategoryId id, EmbroideryCategoryName name) {
        notNull(id, "Embroidery category id must be given");
        notNull(name, "Embroidery category name must be given");

        return new EmbroideryCategory(id, name);
    }

}
