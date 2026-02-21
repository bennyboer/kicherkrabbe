package de.bennyboer.kicherkrabbe.offers.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.persistence.categories.OfferCategoryRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoOfferCategoryRepo implements OfferCategoryRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoOfferCategoryRepo(ReactiveMongoTemplate template) {
        this("offers_categories", template);
    }

    public MongoOfferCategoryRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<OfferCategory> save(OfferCategory category) {
        return template.save(MongoOfferCategoryTransformer.toMongo(category), collectionName)
                .map(MongoOfferCategoryTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(OfferCategoryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Mono<OfferCategory> findById(OfferCategoryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoOfferCategory.class, collectionName)
                .map(MongoOfferCategoryTransformer::fromMongo);
    }

    @Override
    public Flux<OfferCategory> findAll() {
        return template.findAll(MongoOfferCategory.class, collectionName)
                .map(MongoOfferCategoryTransformer::fromMongo);
    }

}
