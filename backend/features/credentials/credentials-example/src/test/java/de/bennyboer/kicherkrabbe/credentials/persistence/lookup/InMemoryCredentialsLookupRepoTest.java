package de.bennyboer.kicherkrabbe.credentials.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.inmemory.InMemoryCredentialsLookupRepo;

public class InMemoryCredentialsLookupRepoTest extends CredentialsLookupRepoTest {

    @Override
    protected CredentialsLookupRepo createRepo() {
        return new InMemoryCredentialsLookupRepo();
    }

}
