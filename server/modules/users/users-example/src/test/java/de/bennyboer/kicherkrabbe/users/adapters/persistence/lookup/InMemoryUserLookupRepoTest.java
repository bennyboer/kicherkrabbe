package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.inmemory.InMemoryUserLookupRepo;

public class InMemoryUserLookupRepoTest extends UserLookupRepoTest {

    @Override
    protected UserLookupRepo createRepo() {
        return new InMemoryUserLookupRepo();
    }

}
