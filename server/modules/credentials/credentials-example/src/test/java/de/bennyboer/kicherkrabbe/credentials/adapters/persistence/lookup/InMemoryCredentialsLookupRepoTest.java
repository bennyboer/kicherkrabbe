package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.inmemory.InMemoryCredentialsLookupRepo;

public class InMemoryCredentialsLookupRepoTest extends CredentialsLookupRepoTest {

    @Override
    protected CredentialsLookupRepo createRepo() {
        return new InMemoryCredentialsLookupRepo();
    }

}
