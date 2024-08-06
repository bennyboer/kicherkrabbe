package de.bennyboer.kicherkrabbe.patterns.persistence.lookup;

import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.inmemory.InMemoryPatternLookupRepo;

public class InMemoryPatternLookupRepoTest extends PatternLookupRepoTest {

    @Override
    protected PatternLookupRepo createRepo() {
        return new InMemoryPatternLookupRepo();
    }

}
