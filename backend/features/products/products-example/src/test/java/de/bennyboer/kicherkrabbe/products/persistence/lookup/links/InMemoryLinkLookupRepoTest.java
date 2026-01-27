package de.bennyboer.kicherkrabbe.products.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.inmemory.InMemoryLinkLookupRepo;

public class InMemoryLinkLookupRepoTest extends LinkLookupRepoTest {

    @Override
    public LinkLookupRepo createRepo() {
        return new InMemoryLinkLookupRepo();
    }

}
