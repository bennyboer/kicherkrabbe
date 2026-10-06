package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.inmemory.InMemoryEmbroideryLookupRepo;

public class InMemoryEmbroideryLookupRepoTest extends EmbroideryLookupRepoTest {

    @Override
    protected EmbroideryLookupRepo createRepo() {
        return new InMemoryEmbroideryLookupRepo();
    }

}
