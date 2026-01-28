package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo.MongoLookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo.MongoProductLookupRepo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoProductLookupRepoTest extends ProductLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoProductLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected ProductLookupRepo createRepo() {
        return new MongoProductLookupRepo("products_product_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupProduct.class)
                .inCollection("products_product_lookup")
                .all()
                .block();
    }

}
