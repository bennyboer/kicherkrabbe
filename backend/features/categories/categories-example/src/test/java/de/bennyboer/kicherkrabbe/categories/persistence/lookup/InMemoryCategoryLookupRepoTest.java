package de.bennyboer.kicherkrabbe.categories.persistence.lookup;

import de.bennyboer.kicherkrabbe.categories.persistence.lookup.inmemory.InMemoryCategoryLookupRepo;

public class InMemoryCategoryLookupRepoTest extends CategoryLookupRepoTest {

    @Override
    protected CategoryLookupRepo createRepo() {
        return new InMemoryCategoryLookupRepo();
    }

}
