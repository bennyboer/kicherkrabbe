package de.bennyboer.kicherkrabbe.offers.persistence.categories;

import de.bennyboer.kicherkrabbe.offers.persistence.categories.inmemory.InMemoryOfferCategoryRepo;

public class InMemoryOfferCategoryRepoTest extends OfferCategoryRepoTest {

    @Override
    protected OfferCategoryRepo createRepo() {
        return new InMemoryOfferCategoryRepo();
    }

}
