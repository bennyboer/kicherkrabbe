package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoProductLookupRepo
        extends MongoEventSourcingReadModelRepo<ProductId, LookupProduct, MongoLookupProduct>
        implements ProductLookupRepo {

    public MongoProductLookupRepo(ReactiveMongoTemplate template) {
        this("products_product_lookup", template);
    }

    public MongoProductLookupRepo(
            String collectionName,
            ReactiveMongoTemplate template
    ) {
        super(collectionName, template, new MongoLookupProductSerializer());
    }

    @Override
    protected String stringifyId(ProductId productId) {
        return productId.getValue();
    }

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupProduct.class, collectionName)
                .map(serializer::deserialize);
    }

}
