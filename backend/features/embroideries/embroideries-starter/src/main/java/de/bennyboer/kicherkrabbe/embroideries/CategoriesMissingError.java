package de.bennyboer.kicherkrabbe.embroideries;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CategoriesMissingError extends Exception {

    private final Set<EmbroideryCategoryId> missingCategories;

    public CategoriesMissingError(Set<EmbroideryCategoryId> missingCategories) {
        super("Categories are missing: " + missingCategories.stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(Collectors.joining(", ")));

        this.missingCategories = missingCategories;
    }

}
