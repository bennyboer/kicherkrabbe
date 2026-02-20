package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProductPage;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

import static org.springframework.data.domain.Sort.Direction.ASC;
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
    public Mono<LookupProductPage> findAll(String searchTerm, long skip, long limit) {
        Criteria criteria = new Criteria();

        if (searchTerm != null && !searchTerm.isBlank()) {
            String quoted = Pattern.quote(searchTerm);
            criteria = where("number").regex(quoted, "i");
        }

        Query countQuery = Query.query(criteria);
        Query pagedQuery = Query.query(criteria)
                .with(Sort.by(ASC, "number"))
                .skip((int) skip)
                .limit((int) limit);

        return template.count(countQuery, MongoProductForOfferLookup.class, collectionName)
                .flatMap(total -> template.find(pagedQuery, MongoProductForOfferLookup.class, collectionName)
                        .map(serializer::deserialize)
                        .collectList()
                        .map(products -> LookupProductPage.of(skip, limit, total, products)));
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        return Mono.empty();
    }

}
