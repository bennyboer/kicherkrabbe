package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoProductForOfferLookupRepo
        extends MongoEventSourcingReadModelRepo<ProductId, LookupProduct, MongoProductForOfferLookup>
        implements ProductForOfferLookupRepo {

    public MongoProductForOfferLookupRepo(ReactiveMongoTemplate template) {
        this("offers_product_lookup", template);
    }

    public MongoProductForOfferLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoProductForOfferLookupSerializer());
    }

    @Override
    protected String stringifyId(ProductId productId) {
        return productId.getValue();
    }

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        return template.findOne(
                query(where("_id").is(id.getValue())),
                MongoProductForOfferLookup.class,
                collectionName
        ).map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        return Mono.empty();
    }

}
