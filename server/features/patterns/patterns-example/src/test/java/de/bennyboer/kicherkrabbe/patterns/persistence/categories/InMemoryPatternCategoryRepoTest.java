package de.bennyboer.kicherkrabbe.patterns.persistence.categories;

import de.bennyboer.kicherkrabbe.patterns.persistence.categories.inmemory.InMemoryPatternCategoryRepo;

public class InMemoryPatternCategoryRepoTest extends PatternCategoryRepoTest {

    @Override
    protected PatternCategoryRepo createRepo() {
        return new InMemoryPatternCategoryRepo();
    }

}
