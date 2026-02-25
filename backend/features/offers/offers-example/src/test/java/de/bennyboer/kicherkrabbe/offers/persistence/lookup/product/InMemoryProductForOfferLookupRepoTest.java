package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.inmemory.InMemoryProductForOfferLookupRepo;

public class InMemoryProductForOfferLookupRepoTest extends ProductForOfferLookupRepoTest {

    @Override
    protected ProductForOfferLookupRepo createRepo() {
        return new InMemoryProductForOfferLookupRepo();
    }

}
