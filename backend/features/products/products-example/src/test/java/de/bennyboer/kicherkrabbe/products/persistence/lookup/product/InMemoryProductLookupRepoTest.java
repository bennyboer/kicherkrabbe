package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.inmemory.InMemoryProductLookupRepo;

public class InMemoryProductLookupRepoTest extends ProductLookupRepoTest {

    @Override
    protected ProductLookupRepo createRepo() {
        return new InMemoryProductLookupRepo();
    }

}
