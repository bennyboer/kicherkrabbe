package de.bennyboer.kicherkrabbe.colors.persistence.lookup;

import de.bennyboer.kicherkrabbe.colors.persistence.lookup.inmemory.InMemoryColorLookupRepo;

public class InMemoryColorLookupRepoTest extends ColorLookupRepoTest {

    @Override
    protected ColorLookupRepo createRepo() {
        return new InMemoryColorLookupRepo();
    }

}
