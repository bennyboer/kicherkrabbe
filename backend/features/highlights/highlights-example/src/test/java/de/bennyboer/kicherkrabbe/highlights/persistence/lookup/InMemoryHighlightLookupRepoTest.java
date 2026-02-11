package de.bennyboer.kicherkrabbe.highlights.persistence.lookup;

import de.bennyboer.kicherkrabbe.highlights.persistence.lookup.inmemory.InMemoryHighlightLookupRepo;

public class InMemoryHighlightLookupRepoTest extends HighlightLookupRepoTest {

    @Override
    protected HighlightLookupRepo createRepo() {
        return new InMemoryHighlightLookupRepo();
    }

}
