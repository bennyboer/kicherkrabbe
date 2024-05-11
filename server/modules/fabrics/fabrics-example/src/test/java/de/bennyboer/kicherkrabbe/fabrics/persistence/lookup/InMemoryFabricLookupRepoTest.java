package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.fabrics.persistence.lookup.inmemory.InMemoryFabricLookupRepo;

public class InMemoryFabricLookupRepoTest extends FabricLookupRepoTest {

    @Override
    protected FabricLookupRepo createRepo() {
        return new InMemoryFabricLookupRepo();
    }

}
