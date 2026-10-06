package de.bennyboer.kicherkrabbe.embroideries.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.EmbroideryCategoryRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoEmbroideryCategoryRepo implements EmbroideryCategoryRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoEmbroideryCategoryRepo(ReactiveMongoTemplate template) {
        this("embroideries_categories", template);
    }

    public MongoEmbroideryCategoryRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<EmbroideryCategory> save(EmbroideryCategory category) {
        return template.save(MongoEmbroideryCategoryTransformer.toMongo(category), collectionName)
                .map(MongoEmbroideryCategoryTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(EmbroideryCategoryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<EmbroideryCategory> findByIds(Set<EmbroideryCategoryId> ids) {
        Set<String> categoryIds = ids.stream()
                .map(EmbroideryCategoryId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(categoryIds);
        Query query = query(criteria);

        return template.find(query, MongoEmbroideryCategory.class, collectionName)
                .map(MongoEmbroideryCategoryTransformer::fromMongo);
    }

    @Override
    public Flux<EmbroideryCategory> findAll() {
        return template.findAll(MongoEmbroideryCategory.class, collectionName)
                .map(MongoEmbroideryCategoryTransformer::fromMongo);
    }

}
