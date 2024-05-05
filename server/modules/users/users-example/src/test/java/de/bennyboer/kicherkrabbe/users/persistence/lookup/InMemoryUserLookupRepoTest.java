package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.persistence.lookup.inmemory.InMemoryUserLookupRepo;

public class InMemoryUserLookupRepoTest extends UserLookupRepoTest {

    @Override
    protected UserLookupRepo createRepo() {
        return new InMemoryUserLookupRepo();
    }

}
