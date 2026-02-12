package de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links;

import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.inmemory.InMemoryLinkLookupRepo;

public class InMemoryLinkLookupRepoTest extends LinkLookupRepoTest {

    @Override
    protected LinkLookupRepo createRepo() {
        return new InMemoryLinkLookupRepo();
    }

}
