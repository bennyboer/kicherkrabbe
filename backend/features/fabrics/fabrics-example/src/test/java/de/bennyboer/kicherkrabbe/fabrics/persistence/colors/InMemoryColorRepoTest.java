package de.bennyboer.kicherkrabbe.fabrics.persistence.colors;

import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.inmemory.InMemoryColorRepo;

public class InMemoryColorRepoTest extends ColorRepoTest {

    @Override
    protected ColorRepo createRepo() {
        return new InMemoryColorRepo();
    }

}
