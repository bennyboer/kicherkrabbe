package de.bennyboer.kicherkrabbe.patterns;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CategoriesMissingError extends Exception {

    private final Set<PatternCategoryId> missingCategories;

    public CategoriesMissingError(Set<PatternCategoryId> missingCategories) {
        super("Categories are missing: " + missingCategories.stream()
                .map(PatternCategoryId::getValue)
                .collect(Collectors.joining(", ")));

        this.missingCategories = missingCategories;
    }

}
