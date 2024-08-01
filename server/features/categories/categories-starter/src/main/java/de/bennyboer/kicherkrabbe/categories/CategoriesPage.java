package de.bennyboer.kicherkrabbe.categories;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CategoriesPage {

    long skip;

    long limit;

    long total;

    List<CategoryDetails> results;

    public static CategoriesPage of(long skip, long limit, long total, List<CategoryDetails> results) {
        notNull(results, "Results must be given");

        return new CategoriesPage(skip, limit, total, results);
    }

}
