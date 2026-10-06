package de.bennyboer.kicherkrabbe.embroideries.persistence.categories;

import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.inmemory.InMemoryEmbroideryCategoryRepo;

public class InMemoryEmbroideryCategoryRepoTest extends EmbroideryCategoryRepoTest {

    @Override
    protected EmbroideryCategoryRepo createRepo() {
        return new InMemoryEmbroideryCategoryRepo();
    }

}
