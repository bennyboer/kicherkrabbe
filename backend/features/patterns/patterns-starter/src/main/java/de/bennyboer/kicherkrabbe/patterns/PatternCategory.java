package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternCategory {

    PatternCategoryId id;

    PatternCategoryName name;

    public static PatternCategory of(PatternCategoryId id, PatternCategoryName name) {
        notNull(id, "Pattern category id must be given");
        notNull(name, "Pattern category name must be given");

        return new PatternCategory(id, name);
    }

}
