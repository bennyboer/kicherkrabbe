package de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup;

import de.bennyboer.kicherkrabbe.fabrictypes.persistence.lookup.inmemory.InMemoryFabricTypeLookupRepo;

public class InMemoryFabricTypeLookupRepoTest extends FabricTypeLookupRepoTest {

    @Override
    protected FabricTypeLookupRepo createRepo() {
        return new InMemoryFabricTypeLookupRepo();
    }

}
