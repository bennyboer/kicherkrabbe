package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.offers.persistence.lookup.inmemory.InMemoryOfferLookupRepo;

public class InMemoryOfferLookupRepoTest extends OfferLookupRepoTest {

    @Override
    protected OfferLookupRepo createRepo() {
        return new InMemoryOfferLookupRepo();
    }

}
