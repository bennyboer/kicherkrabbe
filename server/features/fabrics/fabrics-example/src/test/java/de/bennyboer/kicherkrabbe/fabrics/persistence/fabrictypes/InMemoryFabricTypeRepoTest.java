package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.inmemory.InMemoryFabricTypeRepo;

public class InMemoryFabricTypeRepoTest extends FabricTypeRepoTest {

    @Override
    protected FabricTypeRepo createRepo() {
        return new InMemoryFabricTypeRepo();
    }

}
